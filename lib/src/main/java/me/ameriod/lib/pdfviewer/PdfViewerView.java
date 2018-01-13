package me.ameriod.lib.pdfviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * View to display a PDF file using the {@link PdfRenderer}, {@link ViewPager} and {@link PhotoView}
 * <p>
 * Created by parker on 1/12/18.
 */
public class PdfViewerView extends ViewPager {

    private PdfRenderer pdfRender;

    public PdfViewerView(Context context) {
        super(context);
    }

    public PdfViewerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    /**
     * Uses the {@link ParcelFileDescriptor} directly, see {@link PdfRenderer(ParcelFileDescriptor)}
     *
     * @param input {@link ParcelFileDescriptor}
     */
    public void setPdf(@NonNull ParcelFileDescriptor input) {
        if (pdfRender != null) {
            pdfRender.close();
        }
        try {
            pdfRender = new PdfRenderer(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setAdapter(new PdfAdapter(getContext(), pdfRender));
    }

    /**
     * Sets the pdf with the file uses {@link #setPdf(ParcelFileDescriptor)} internally
     *
     * @param file of the pdf to load
     */
    public void setPdf(@NonNull File file) {
        try {
            setPdf(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets a pdf to view from the assets directory
     *
     * @param assetFile the path and name of the asset to load
     */
    public void setPdfFromAsset(@NonNull String assetFile) {
        try {
            Context context = getContext();
            File file = new File(context.getCacheDir(), assetFile);
            if (!file.exists()) {
                // copy to the assets dir
                InputStream asset = context.getAssets().open(assetFile);
                FileOutputStream output = new FileOutputStream(file);
                final byte[] buffer = new byte[1024];
                int size;
                while ((size = asset.read(buffer)) != -1) {
                    output.write(buffer, 0, size);
                }
                asset.close();
                output.close();
            }
            setPdf(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets a pdf to view from a byte array
     *
     * @param bytes for the file
     * @param name  the name of the file to save in the app's cache dir
     */
    public void setPdfFromBytes(@NonNull byte[] bytes, @NonNull String name) {
        File file = new File(getContext().getCacheDir(), name);
        try {
            if (!file.exists()) {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                bos.write(bytes);
                bos.flush();
                bos.close();
            }
            setPdf(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The current page count
     */
    public int getPageCount() {
        return pdfRender == null ? 0 : pdfRender.getPageCount();
    }

    /**
     * @return The current page via {@link #getCurrentItem()}
     */
    public int getCurrentPage() {
        return getCurrentItem();
    }

    private static class PdfAdapter extends PagerAdapter {

        @NonNull
        private final PdfRenderer renderer;
        private final int count;
        private PdfRenderer.Page currentPage;
        @NonNull
        private final Context context;

        private PdfAdapter(@NonNull Context context, @NonNull PdfRenderer renderer) {
            this.context = context;
            this.count = renderer.getPageCount();
            this.renderer = renderer;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (currentPage != null) {
                currentPage.close();
            }
            currentPage = renderer.openPage(position);

            Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(),
                    Bitmap.Config.ARGB_8888);
            currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            PhotoView photoView = new PhotoView(context);

            photoView.setImageBitmap(bitmap);

            container.addView(photoView);

            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            try {
                if (currentPage != null) {
                    currentPage.close();
                }
            } catch (Exception e) {
                // no op, need to make sure it is closed
            }

            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}

