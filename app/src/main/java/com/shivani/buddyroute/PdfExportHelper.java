package com.shivani.buddyroute;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;

import com.shivani.buddyroute.model.Trip;
import com.shivani.buddyroute.model.TripNote;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfExportHelper {

    // Theme colors per trip type
    private static int[] getThemeColors(String tripType) {
        if (tripType == null) tripType = "Road";
        switch (tripType) {
            case "Beach":
                // Ocean blue + sandy yellow
                return new int[]{
                        Color.parseColor("#0077B6"),
                        Color.parseColor("#00B4D8"),
                        Color.parseColor("#F4A261")};
            case "Trek":
                // Forest green + mountain grey
                return new int[]{
                        Color.parseColor("#2D6A4F"),
                        Color.parseColor("#52B788"),
                        Color.parseColor("#B7C9B7")};
            case "City":
                // Urban purple + neon accent
                return new int[]{
                        Color.parseColor("#3A0CA3"),
                        Color.parseColor("#7209B7"),
                        Color.parseColor("#F72585")};
            case "Road":
            default:
                // Sunset orange + red
                return new int[]{
                        Color.parseColor("#E85D04"),
                        Color.parseColor("#F48C06"),
                        Color.parseColor("#FAA307")};
        }
    }

    private static String getThemeEmoji(String tripType) {
        if (tripType == null) return "🗺️";
        switch (tripType) {
            case "Beach": return "🏖️";
            case "Trek":  return "🥾";
            case "City":  return "🏙️";
            default:      return "🚗";
        }
    }

    private static String getThemePattern(String tripType) {
        if (tripType == null) return "waves";
        switch (tripType) {
            case "Beach": return "waves";
            case "Trek":  return "mountains";
            case "City":  return "grid";
            default:      return "road";
        }
    }

    public static File exportTrip(Context context,
                                  Trip trip, List<TripNote> notes) throws Exception {

        // Output file
        File outputDir = context.getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS);
        if (outputDir == null) outputDir = context.getFilesDir();
        if (!outputDir.exists()) outputDir.mkdirs();

        String fileName = "BuddyRoute_" +
                trip.name.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";
        File outputFile = new File(outputDir, fileName);

        // PDF page size A4 in points (72 pts per inch)
        // A4 = 595 x 842 pts
        int pageWidth = 595;
        int pageHeight = 842;

        android.graphics.pdf.PdfDocument pdfDocument =
                new android.graphics.pdf.PdfDocument();

        int[] colors = getThemeColors(trip.tripType);
        String emoji = getThemeEmoji(trip.tripType);

        // ── PAGE 1 — COVER ─────────────────────────────────

        android.graphics.pdf.PdfDocument.PageInfo coverInfo =
                new android.graphics.pdf.PdfDocument.PageInfo.Builder(
                        pageWidth, pageHeight, 1).create();
        android.graphics.pdf.PdfDocument.Page coverPage =
                pdfDocument.startPage(coverInfo);
        Canvas cover = coverPage.getCanvas();

        drawCoverPage(cover, trip, notes, colors,
                emoji, pageWidth, pageHeight);

        pdfDocument.finishPage(coverPage);

        // ── PAGE 2+ — NOTES ────────────────────────────────

        if (notes != null && !notes.isEmpty()) {
            // Draw notes — multiple per page
            int pageNum = 2;
            int notesPerPage = 3;
            int totalPages = (int) Math.ceil(
                    (double) notes.size() / notesPerPage);

            for (int p = 0; p < totalPages; p++) {
                android.graphics.pdf.PdfDocument.PageInfo notePageInfo =
                        new android.graphics.pdf.PdfDocument.PageInfo.Builder(
                                pageWidth, pageHeight, pageNum++).create();
                android.graphics.pdf.PdfDocument.Page notePage =
                        pdfDocument.startPage(notePageInfo);
                Canvas noteCanvas = notePage.getCanvas();

                int startIdx = p * notesPerPage;
                int endIdx = Math.min(startIdx + notesPerPage,
                        notes.size());
                List<TripNote> pageNotes =
                        notes.subList(startIdx, endIdx);

                drawNotesPage(noteCanvas, trip, pageNotes,
                        colors, p + 1, totalPages,
                        pageWidth, pageHeight, context);

                pdfDocument.finishPage(notePage);
            }
        }

        // ── LAST PAGE — SUMMARY ────────────────────────────

        android.graphics.pdf.PdfDocument.PageInfo summaryInfo =
                new android.graphics.pdf.PdfDocument.PageInfo.Builder(
                        pageWidth, pageHeight, 99).create();
        android.graphics.pdf.PdfDocument.Page summaryPage =
                pdfDocument.startPage(summaryInfo);
        Canvas summary = summaryPage.getCanvas();

        drawSummaryPage(summary, trip, notes,
                colors, emoji, pageWidth, pageHeight);

        pdfDocument.finishPage(summaryPage);

        // Write to file
        FileOutputStream fos = new FileOutputStream(outputFile);
        pdfDocument.writeTo(fos);
        fos.close();
        pdfDocument.close();

        return outputFile;
    }

    // ── COVER PAGE DRAWING ─────────────────────────────────

    private static void drawCoverPage(Canvas canvas, Trip trip,
                                      List<TripNote> notes, int[] colors,
                                      String emoji, int w, int h) {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Background gradient
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, h * 0.65f,
                colors[0], colors[1],
                Shader.TileMode.CLAMP);
        paint.setShader(gradient);
        canvas.drawRect(0, 0, w, h * 0.65f, paint);
        paint.setShader(null);

        // Bottom white section
        paint.setColor(Color.WHITE);
        canvas.drawRect(0, h * 0.62f, w, h, paint);

        // Decorative pattern overlay
        drawPattern(canvas, trip.tripType, colors[2],
                w, (int)(h * 0.65f));

        // Decorative circles
        paint.setColor(Color.WHITE);
        paint.setAlpha(20);
        canvas.drawCircle(w * 0.85f, h * 0.1f, 120, paint);
        canvas.drawCircle(w * 0.1f, h * 0.5f, 80, paint);
        paint.setAlpha(255);

        // BuddyRoute label
        paint.setColor(Color.WHITE);
        paint.setAlpha(180);
        paint.setTextSize(14);
        paint.setTypeface(Typeface.create(
                Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("BuddyRoute", 40, 50, paint);
        paint.setAlpha(255);

        // Emoji large
        paint.setTextSize(72);
        canvas.drawText(emoji, w/2f - 36, h * 0.2f, paint);

        // Trip name
        paint.setColor(Color.WHITE);
        paint.setTextSize(36);
        paint.setTypeface(Typeface.create(
                Typeface.DEFAULT, Typeface.BOLD));
        drawCenteredText(canvas, trip.name,
                h * 0.33f, paint, w);

        // Destination
        paint.setTextSize(16);
        paint.setAlpha(200);
        drawCenteredText(canvas, "📍 " + trip.destination,
                h * 0.4f, paint, w);
        paint.setAlpha(255);

        // Curved separator
        paint.setColor(Color.WHITE);
        android.graphics.Path curve =
                new android.graphics.Path();
        curve.moveTo(0, h * 0.58f);
        curve.quadTo(w/2f, h * 0.68f, w, h * 0.58f);
        curve.lineTo(w, h);
        curve.lineTo(0, h);
        curve.close();
        canvas.drawPath(curve, paint);

        // Stats boxes
        float boxY = h * 0.68f;
        float boxW = (w - 80f) / 3f;

        drawStatBox(canvas, "Distance",
                formatDist(trip.totalDistance),
                40, boxY, boxW, colors[0]);
        drawStatBox(canvas, "Notes",
                String.valueOf(trip.notesCount),
                40 + boxW + 20, boxY, boxW, colors[0]);
        drawStatBox(canvas, "Date",
                new SimpleDateFormat("dd MMM yy",
                        Locale.getDefault())
                        .format(new Date(trip.startTime)),
                40 + (boxW + 20) * 2, boxY, boxW, colors[0]);

        // Trip type badge
        paint.setColor(colors[0]);
        RectF badge = new RectF(w/2f - 50,
                h * 0.88f, w/2f + 50, h * 0.88f + 30);
        canvas.drawRoundRect(badge, 15, 15, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(13);
        paint.setTypeface(Typeface.create(
                Typeface.DEFAULT, Typeface.BOLD));
        drawCenteredText(canvas,
                emoji + " " + trip.tripType + " Trip",
                h * 0.88f + 20, paint, w);
    }

    // ── NOTES PAGE DRAWING ─────────────────────────────────

    private static void drawNotesPage(Canvas canvas, Trip trip,
                                      List<TripNote> pageNotes, int[] colors,
                                      int pageNum, int totalPages,
                                      int w, int h, Context context) {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // White background
        paint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, w, h, paint);

        // Top accent bar
        paint.setColor(colors[0]);
        canvas.drawRect(0, 0, w, 8, paint);

        // Header
        paint.setColor(colors[0]);
        paint.setTextSize(22);
        paint.setTypeface(Typeface.create(
                Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Trip Journal", 40, 50, paint);

        // Page number
        paint.setColor(Color.GRAY);
        paint.setTextSize(11);
        paint.setTypeface(Typeface.DEFAULT);
        String pageText = "Journal " + pageNum +
                " of " + totalPages;
        canvas.drawText(pageText,
                w - 40 - paint.measureText(pageText), 50, paint);

        // Trip name subtitle
        paint.setColor(Color.GRAY);
        paint.setTextSize(12);
        canvas.drawText(trip.name + " • " + trip.destination,
                40, 70, paint);

        // Divider
        paint.setColor(colors[0]);
        paint.setAlpha(60);
        canvas.drawRect(40, 78, w - 40, 80, paint);
        paint.setAlpha(255);

        // Draw each note
        float yPos = 100;
        SimpleDateFormat sdf = new SimpleDateFormat(
                "hh:mm a, dd MMM", Locale.getDefault());

        for (int i = 0; i < pageNotes.size(); i++) {
            TripNote note = pageNotes.get(i);
            float noteHeight = note.photoPath != null ?
                    220 : 120;

            // Note card background
            paint.setColor(Color.parseColor("#F8F9FA"));
            paint.setShadowLayer(4, 0, 2,
                    Color.parseColor("#22000000"));
            RectF cardRect = new RectF(
                    30, yPos, w - 30, yPos + noteHeight);
            canvas.drawRoundRect(cardRect, 12, 12, paint);
            paint.setShadowLayer(0, 0, 0, 0);

            // Left color accent bar
            paint.setColor(colors[0]);
            RectF accentBar = new RectF(
                    30, yPos, 38, yPos + noteHeight);
            canvas.drawRoundRect(accentBar, 6, 6, paint);

            // Mood emoji
            paint.setTextSize(24);
            canvas.drawText(getMoodEmoji(note.mood),
                    50, yPos + 30, paint);

            // Time
            paint.setColor(Color.GRAY);
            paint.setTextSize(10);
            paint.setTypeface(Typeface.DEFAULT);
            canvas.drawText(sdf.format(
                            new Date(note.timestamp)),
                    85, yPos + 20, paint);

            // Note text — wrap long text
            paint.setColor(Color.parseColor("#333333"));
            paint.setTextSize(13);
            paint.setTypeface(Typeface.DEFAULT);
            drawWrappedText(canvas, note.noteText,
                    50, yPos + 45, w - 70, paint, 3);

            // Photo if exists
            if (note.photoPath != null) {
                try {
                    Bitmap photo = BitmapFactory.decodeFile(
                            note.photoPath);
                    if (photo != null) {
                        // Scale photo to fit
                        float photoW = w - 80f;
                        float photoH = 120f;
                        float scaleW = photoW / photo.getWidth();
                        float scaleH = photoH / photo.getHeight();
                        float scale = Math.min(scaleW, scaleH);

                        int scaledW = (int)(photo.getWidth() * scale);
                        int scaledH = (int)(photo.getHeight() * scale);

                        Bitmap scaled = Bitmap.createScaledBitmap(
                                photo, scaledW, scaledH, true);

                        RectF photoRect = new RectF(
                                50, yPos + 90,
                                50 + scaledW, yPos + 90 + scaledH);
                        canvas.drawBitmap(scaled,
                                null, photoRect, null);
                        photo.recycle();
                        scaled.recycle();
                    }
                } catch (Exception e) {
                    // Skip if photo fails
                }
            }

            yPos += noteHeight + 20;
        }

        // Bottom footer
        paint.setColor(Color.LTGRAY);
        canvas.drawRect(0, h - 25, w, h - 24, paint);
        paint.setColor(Color.GRAY);
        paint.setTextSize(9);
        paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText("Generated by BuddyRoute 🗺️",
                40, h - 10, paint);
        canvas.drawText(trip.name,
                w - 40 - paint.measureText(trip.name),
                h - 10, paint);
    }

    // ── SUMMARY PAGE DRAWING ───────────────────────────────

    private static void drawSummaryPage(Canvas canvas, Trip trip,
                                        List<TripNote> notes, int[] colors,
                                        String emoji, int w, int h) {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Dark background
        paint.setColor(colors[0]);
        canvas.drawRect(0, 0, w, h, paint);

        // Subtle pattern
        drawPattern(canvas, trip.tripType,
                colors[2], w, h);

        // Decorative circles
        paint.setColor(Color.WHITE);
        paint.setAlpha(10);
        canvas.drawCircle(w * 0.9f, h * 0.15f, 150, paint);
        canvas.drawCircle(w * 0.05f, h * 0.8f, 100, paint);
        paint.setAlpha(255);

        // Title
        paint.setColor(Color.WHITE);
        paint.setTextSize(28);
        paint.setTypeface(Typeface.create(
                Typeface.DEFAULT, Typeface.BOLD));
        drawCenteredText(canvas, "Trip Complete! " + emoji,
                h * 0.12f, paint, w);

        // Subtitle
        paint.setTextSize(14);
        paint.setAlpha(180);
        drawCenteredText(canvas,
                trip.name + " • " + trip.destination,
                h * 0.18f, paint, w);
        paint.setAlpha(255);

        // Big stats grid
        float gridTop = h * 0.25f;
        float cellW = (w - 80f) / 2f;
        float cellH = 100f;
        float gap = 20f;

        // Calculate duration
        long duration = trip.endTime > 0 ?
                trip.endTime - trip.startTime : 0;
        long hours = duration / (1000 * 60 * 60);
        long mins = (duration / (1000 * 60)) % 60;
        String durationStr = hours > 0 ?
                hours + "h " + mins + "m" : mins + " mins";

        // Count photos
        int photoCount = 0;
        int voiceCount = 0;
        if (notes != null) {
            for (TripNote n : notes) {
                if (n.photoPath != null) photoCount++;
                if (n.voiceNotePath != null) voiceCount++;
            }
        }

        drawBigStatCell(canvas,
                "📏", "Distance",
                formatDist(trip.totalDistance),
                40, gridTop, cellW, cellH, colors[1], paint);
        drawBigStatCell(canvas,
                "📝", "Journal Entries",
                String.valueOf(trip.notesCount),
                40 + cellW + gap, gridTop,
                cellW, cellH, colors[1], paint);
        drawBigStatCell(canvas,
                "📷", "Photos",
                String.valueOf(photoCount),
                40, gridTop + cellH + gap,
                cellW, cellH, colors[1], paint);
        drawBigStatCell(canvas,
                "⏱️", "Duration",
                trip.endTime > 0 ? durationStr : "Active",
                40 + cellW + gap,
                gridTop + cellH + gap,
                cellW, cellH, colors[1], paint);

        // Mood summary
        if (notes != null && !notes.isEmpty()) {
            paint.setColor(Color.WHITE);
            paint.setAlpha(180);
            paint.setTextSize(12);
            paint.setTypeface(Typeface.DEFAULT);
            drawCenteredText(canvas, "Your mood journey",
                    h * 0.65f, paint, w);
            paint.setAlpha(255);

            // Show all mood emojis
            StringBuilder moodLine = new StringBuilder();
            for (TripNote note : notes) {
                moodLine.append(getMoodEmoji(note.mood))
                        .append(" ");
            }
            paint.setTextSize(24);
            drawCenteredText(canvas,
                    moodLine.toString().trim(),
                    h * 0.72f, paint, w);
        }

        // Date range
        SimpleDateFormat sdf = new SimpleDateFormat(
                "dd MMM yyyy", Locale.getDefault());
        paint.setColor(Color.WHITE);
        paint.setAlpha(150);
        paint.setTextSize(13);
        paint.setTypeface(Typeface.DEFAULT);
        String dateStr = sdf.format(new Date(trip.startTime));
        drawCenteredText(canvas, dateStr, h * 0.82f, paint, w);

        // BuddyRoute branding at bottom
        paint.setAlpha(120);
        paint.setTextSize(11);
        drawCenteredText(canvas,
                "Created with BuddyRoute — Your travel stories, mapped",
                h * 0.94f, paint, w);
    }

    // ── HELPER METHODS ─────────────────────────────────────

    private static void drawPattern(Canvas canvas,
                                    String tripType, int color, int w, int h) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setAlpha(25);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2);

        if ("Beach".equals(tripType)) {
            // Wave pattern
            for (int y = 0; y < h; y += 40) {
                android.graphics.Path wave =
                        new android.graphics.Path();
                wave.moveTo(0, y);
                for (int x = 0; x < w; x += 60) {
                    wave.quadTo(x + 15, y - 15,
                            x + 30, y);
                    wave.quadTo(x + 45, y + 15,
                            x + 60, y);
                }
                canvas.drawPath(wave, p);
            }
        } else if ("Trek".equals(tripType)) {
            // Mountain triangles
            for (int x = -40; x < w; x += 80) {
                for (int y = 0; y < h; y += 80) {
                    android.graphics.Path tri =
                            new android.graphics.Path();
                    tri.moveTo(x, y + 60);
                    tri.lineTo(x + 40, y);
                    tri.lineTo(x + 80, y + 60);
                    tri.close();
                    canvas.drawPath(tri, p);
                }
            }
        } else if ("City".equals(tripType)) {
            // Grid pattern
            for (int x = 0; x < w; x += 30) {
                canvas.drawLine(x, 0, x, h, p);
            }
            for (int y = 0; y < h; y += 30) {
                canvas.drawLine(0, y, w, y, p);
            }
        } else {
            // Road dashes
            p.setPathEffect(
                    new android.graphics.DashPathEffect(
                            new float[]{20, 10}, 0));
            canvas.drawLine(w/2f, 0, w/2f, h, p);
            p.setPathEffect(null);
        }
    }

    private static void drawStatBox(Canvas canvas,
                                    String label, String value,
                                    float x, float y, float w, int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.WHITE);
        p.setShadowLayer(6, 0, 3,
                Color.parseColor("#33000000"));
        RectF rect = new RectF(x, y, x + w, y + 70);
        canvas.drawRoundRect(rect, 10, 10, p);
        p.setShadowLayer(0, 0, 0, 0);

        p.setColor(color);
        p.setTextSize(18);
        p.setTypeface(Typeface.create(
                Typeface.DEFAULT, Typeface.BOLD));
        drawTextInBox(canvas, value,
                x, y, w, 35, p);

        p.setColor(Color.GRAY);
        p.setTextSize(10);
        p.setTypeface(Typeface.DEFAULT);
        drawTextInBox(canvas, label,
                x, y + 35, w, 25, p);
    }

    private static void drawBigStatCell(Canvas canvas,
                                        String icon, String label, String value,
                                        float x, float y, float w, float h,
                                        int color, Paint paint) {
        paint.setColor(Color.parseColor("#33FFFFFF"));
        paint.setShadowLayer(0, 0, 0, 0);
        RectF rect = new RectF(x, y, x + w, y + h);
        canvas.drawRoundRect(rect, 12, 12, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(20);
        canvas.drawText(icon, x + 15, y + 28, paint);

        paint.setTextSize(22);
        paint.setTypeface(Typeface.create(
                Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(value, x + 15, y + 58, paint);

        paint.setTextSize(11);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setAlpha(180);
        canvas.drawText(label, x + 15, y + 78, paint);
        paint.setAlpha(255);
    }

    private static void drawCenteredText(Canvas canvas,
                                         String text, float y, Paint paint, int w) {
        float textWidth = paint.measureText(text);
        canvas.drawText(text, (w - textWidth) / 2f, y, paint);
    }

    private static void drawTextInBox(Canvas canvas,
                                      String text, float x, float y,
                                      float w, float h, Paint paint) {
        float textWidth = paint.measureText(text);
        canvas.drawText(text,
                x + (w - textWidth) / 2f,
                y + h / 2f + paint.getTextSize() / 3f,
                paint);
    }

    private static void drawWrappedText(Canvas canvas,
                                        String text, float x, float y,
                                        float maxWidth, Paint paint, int maxLines) {
        if (text == null) return;
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        float lineHeight = paint.getTextSize() + 5;
        int lineCount = 0;

        for (String word : words) {
            String testLine = line + word + " ";
            if (paint.measureText(testLine) > maxWidth
                    && line.length() > 0) {
                canvas.drawText(line.toString().trim(),
                        x, y + lineCount * lineHeight, paint);
                line = new StringBuilder(word + " ");
                lineCount++;
                if (lineCount >= maxLines) {
                    canvas.drawText("...",
                            x, y + lineCount * lineHeight, paint);
                    return;
                }
            } else {
                line.append(word).append(" ");
            }
        }
        if (line.length() > 0) {
            canvas.drawText(line.toString().trim(),
                    x, y + lineCount * lineHeight, paint);
        }
    }

    private static String formatDist(float km) {
        if (km < 1)
            return (int)(km * 1000) + " m";
        return String.format(Locale.getDefault(),
                "%.1f km", km);
    }

    private static String getMoodEmoji(String mood) {
        if (mood == null) return "😄";
        switch (mood) {
            case "amazed":   return "🤩";
            case "tired":    return "😴";
            case "hungry":   return "🍔";
            case "peaceful": return "😌";
            default:         return "😄";
        }
    }
}