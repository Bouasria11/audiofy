package com.example.audiofy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvPdfName, tvExtractedText;
    private Button btnSelectPdf, btnPlay, btnStop, btnClear, btnHistory;
    private TextToSpeech tts;
    private DatabaseHelper dbHelper;
    private String extractedText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize PDFBox
        PDFBoxResourceLoader.init(getApplicationContext());
        dbHelper = new DatabaseHelper(this);

        tvPdfName = findViewById(R.id.tvPdfName);
        tvExtractedText = findViewById(R.id.tvExtractedText);
        btnSelectPdf = findViewById(R.id.btnSelectPdf);
        btnPlay = findViewById(R.id.btnPlay);
        btnStop = findViewById(R.id.btnStop);
        btnClear = findViewById(R.id.btnClear);
        btnHistory = findViewById(R.id.btnHistory);

        // Initialize TextToSpeech
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.US);
            }
        });

        // PDF Selection Launcher
        ActivityResultLauncher<Intent> pdfPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        handlePdfSelection(uri);
                    }
                }
        );

        btnSelectPdf.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("application/pdf");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            pdfPickerLauncher.launch(intent);
        });

        btnPlay.setOnClickListener(v -> {
            if (!extractedText.isEmpty()) {
                tts.speak(extractedText, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                Toast.makeText(this, "No text to play", Toast.LENGTH_SHORT).show();
            }
        });

        btnStop.setOnClickListener(v -> {
            if (tts.isSpeaking()) {
                tts.stop();
            }
        });

        btnClear.setOnClickListener(v -> {
            extractedText = "";
            tvExtractedText.setText("Extracted text will appear here...");
            tvPdfName.setText("No file selected");
            if (tts.isSpeaking()) tts.stop();
        });

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }

    private void handlePdfSelection(Uri uri) {
        try {
            String fileName = "Selected PDF"; // Simplified for this example
            tvPdfName.setText(fileName);

            InputStream inputStream = getContentResolver().openInputStream(uri);
            PDDocument document = PDDocument.load(inputStream);
            PDFTextStripper stripper = new PDFTextStripper();
            extractedText = stripper.getText(document);
            document.close();

            if (extractedText.trim().isEmpty()) {
                tvExtractedText.setText("The selected PDF has no extractable text.");
                extractedText = "";
            } else {
                tvExtractedText.setText(extractedText);
                // Save to History
                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                dbHelper.addHistory(fileName, uri.toString(), date);
            }

        } catch (IOException e) {
            Toast.makeText(this, "Error reading PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}