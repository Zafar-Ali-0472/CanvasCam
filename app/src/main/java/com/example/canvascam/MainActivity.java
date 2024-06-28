package com.example.canvascam;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 200;
    private static final int SCROLL_INTERVAL = 100; // Interval for scrolling in milliseconds
    private static final int SCROLL_SPEED = 1; // Speed of scrolling in pixels

    private ScrollView scrollView;
    private LinearLayout canvasLayout;
    private Handler scrollHandler;
    private int scrollPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scrollView = findViewById(R.id.scroll_view);
        canvasLayout = findViewById(R.id.canvas_layout);

        // Request camera and gallery permissions if not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_REQUEST_CODE);
        }

        // Set onTouchListener to detect touch events on the screen
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float y = event.getY();
                float screenHeight = scrollView.getHeight();
                float midPoint = screenHeight / 2;

                // Check if the touch event occurred on the bottom half of the screen
                if (y >= midPoint) {
                    openCamera();
                } else {
                    openGallery();
                }
                return true;
            }
        });

        // Start continuous scrolling
        startContinuousScroll();
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                addImageToCanvas(photo);
            } else if (requestCode == GALLERY_REQUEST_CODE) {
                // Handle image selected from gallery
                // Get the image URI from the intent
                // Use MediaStore.Images.Media.getBitmap() to get the bitmap from the URI
                // Then call addImageToCanvas(bitmap) with the bitmap
            }
        }
    }

    private void addImageToCanvas(Bitmap bitmap) {
        // Create a new ImageView for the new image
        ImageView newImageView = new ImageView(this);
        newImageView.setImageBitmap(bitmap);

        // Add the new image at the bottom of the LinearLayout
        canvasLayout.addView(newImageView);

        // Scroll to the bottom of the canvasLayout
        scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Scroll up with animation duration 500ms
                scrollView.smoothScrollBy(0, -canvasLayout.getChildAt(0).getHeight());
                // Remove the top view and add it to the bottom to create a continuous loop
                View topView = canvasLayout.getChildAt(0);
                canvasLayout.removeViewAt(0);
                canvasLayout.addView(topView);
                // Post a delayed action to add the image again after scrolling
                scrollView.postDelayed(this, 3000); // Adjust the delay as needed
            }
        }, 3000); // Adjust the delay as needed
    }


    private void startContinuousScroll() {
        scrollHandler = new Handler();
        scrollHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int lastImageBottom = getLastImageBottom();
                int scrollViewBottom = scrollView.getScrollY() + scrollView.getHeight();

                if (lastImageBottom > scrollViewBottom) {
                    // Scroll the layout by SCROLL_SPEED pixels
                    scrollPosition += SCROLL_SPEED;
                    scrollView.smoothScrollTo(0, scrollPosition);
                    if (scrollPosition >= lastImageBottom) {
                        scrollPosition = 0; // Reset scroll position when reaching the end
                    }
                }

                // Schedule next scroll after SCROLL_INTERVAL milliseconds
                scrollHandler.postDelayed(this, SCROLL_INTERVAL);
            }
        }, SCROLL_INTERVAL);
    }

    private int getLastImageBottom() {
        if (canvasLayout.getChildCount() > 0) {
            View lastChild = canvasLayout.getChildAt(canvasLayout.getChildCount() - 1);
            return lastChild.getBottom();
        }
        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop continuous scrolling when activity is destroyed
        if (scrollHandler != null) {
            scrollHandler.removeCallbacksAndMessages(null);
        }
    }
}

