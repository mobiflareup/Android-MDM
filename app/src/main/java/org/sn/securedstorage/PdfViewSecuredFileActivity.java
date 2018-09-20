package org.sn.securedstorage;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.mobiocean.R;
import com.mobiocean.util.DeBug;
import com.shockwave.pdfium.PdfDocument;

import java.io.InputStream;
import java.util.List;

public class PdfViewSecuredFileActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener {

    PDFView pdfView;
    private String TAG = "SecurePDF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_view_secure_file);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        Context context = this;
        pdfView = (PDFView) findViewById(R.id.pdfView);
        Uri uri = getIntent().getData();
        if (uri != null) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1));
            }
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                pdfView.fromStream(is)
                        .onPageChange(this)
                        .enableAnnotationRendering(true)
                        .onLoad(this)
                        .scrollHandle(new DefaultScrollHandle(this))
                        .load();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, "Undefined error please try again later", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        DeBug.ShowLogD(TAG, "title = " + meta.getTitle());
        DeBug.ShowLogD(TAG, "author = " + meta.getAuthor());
        DeBug.ShowLogD(TAG, "subject = " + meta.getSubject());
        DeBug.ShowLogD(TAG, "keywords = " + meta.getKeywords());
        DeBug.ShowLogD(TAG, "creator = " + meta.getCreator());
        DeBug.ShowLogD(TAG, "producer = " + meta.getProducer());
        DeBug.ShowLogD(TAG, "creationDate = " + meta.getCreationDate());
        DeBug.ShowLogD(TAG, "modDate = " + meta.getModDate());
        printBookmarksTree(pdfView.getTableOfContents(), "-");
    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {
            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));
            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        DeBug.ShowLogD(TAG, "Page " + page + "/" + pageCount);
    }
}
