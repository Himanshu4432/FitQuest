package com.example.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object ScoreCardSharer {

    fun shareDailyScoreCard(
        context: Context,
        profile: UserProfile,
        todayLog: DailyLog,
        breakdown: ScoreBreakdown
    ) {
        val width = 1080
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background gradient
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                Color.parseColor("#0B1117"),
                Color.parseColor("#132538"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Accent top border
        val accentPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), 0f,
                Color.parseColor("#A3FF12"),
                Color.parseColor("#00D4FF"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), 16f, accentPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A3FF12")
            textSize = 54f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#9FB2C8")
            textSize = 34f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val whiteBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F4F8FC")
            textSize = 44f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        canvas.drawText("FITQUEST", 72f, 118f, titlePaint)
        canvas.drawText("DAILY HEALTH SCORE  •  ${todayLog.dateKey}", 72f, 168f, subPaint)
        canvas.drawText("@${profile.username}  •  LVL ${profile.level}", 72f, 232f, whiteBold)

        // Score ring
        val cx = width / 2f
        val cy = 510f
        val radius = 210f
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 36f
            color = Color.parseColor("#1E2C3E")
        }
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 36f
            strokeCap = Paint.Cap.ROUND
            shader = LinearGradient(
                cx - radius, cy - radius, cx + radius, cy + radius,
                Color.parseColor("#A3FF12"),
                Color.parseColor("#00D4FF"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(cx, cy, radius, trackPaint)
        val sweep = (breakdown.total / 1000f).coerceIn(0f, 1f) * 360f
        canvas.drawArc(
            RectF(cx - radius, cy - radius, cx + radius, cy + radius),
            -90f,
            sweep,
            false,
            ringPaint
        )

        val bigScorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F4F8FC")
            textSize = 112f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        val outOfPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A3FF12")
            textSize = 36f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText("${breakdown.total}", cx, cy + 22f, bigScorePaint)
        canvas.drawText("/ 1000 PTS", cx, cy + 78f, outOfPaint)

        // Stat cards
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#172230")
        }
        val metrics = listOf(
            Triple("STEPS", "${todayLog.steps}", "${breakdown.stepsPoints}/250 pts"),
            Triple("WATER", String.format(Locale.US, "%.1fL", todayLog.waterMl / 1000.0), "${breakdown.waterPoints}/100 pts"),
            Triple("RUNNING", String.format(Locale.US, "%.1f km", todayLog.runningKm), "${breakdown.runCyclePoints}/250 pts"),
            Triple("SPORTS", "${todayLog.sportsMinutes} min", "${breakdown.sportsPoints}/250 pts")
        )
        metrics.forEachIndexed { idx, (label, value, pts) ->
            val col = idx % 2
            val row = idx / 2
            val left = 72f + col * 484f
            val top = 790f + row * 190f
            val rect = RectF(left, top, left + 452f, top + 162f)
            canvas.drawRoundRect(rect, 32f, 32f, cardPaint)
            canvas.drawText(label, left + 32f, top + 52f, subPaint)
            canvas.drawText(value, left + 32f, top + 106f, whiteBold)
            val ptsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#00D4FF")
                textSize = 28f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            canvas.drawText(pts, left + 32f, top + 144f, ptsPaint)
        }

        // Footer banner
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#A3FF12")
            textSize = 36f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText(
            "+${todayLog.xpEarnedToday} XP Today  •  ${profile.currentStreak}-Day Streak",
            72f,
            1240f,
            footerPaint
        )
        canvas.drawText("Level Up Your Life with FitQuest", 72f, 1295f, subPaint)

        // Write to cache and share via FileProvider
        val shareDir = File(context.cacheDir, "shared_cards").apply { mkdirs() }
        val file = File(shareDir, "fitquest_score_${todayLog.dateKey}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
        bitmap.recycle()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_TEXT,
                "I scored ${breakdown.total}/1000 on FitQuest today (+${todayLog.xpEarnedToday} XP, ${profile.currentStreak}-day streak)! Level Up Your Life!"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share FitQuest Daily Score"))
    }

    fun shareInviteLink(context: Context, username: String, challengeTitle: String? = null) {
        val text = if (challengeTitle != null) {
            "Join my FitQuest challenge \"$challengeTitle\"! Search @$username in FitQuest to compete for XP & leaderboard ranks. https://fitquest.app/invite/$username"
        } else {
            "Come level up your fitness with me on FitQuest! Add @$username to compete on leaderboards and 7-day step battles. https://fitquest.app/invite/$username"
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Invite Friends to FitQuest"))
    }
}
