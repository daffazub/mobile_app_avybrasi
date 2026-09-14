import 'package:flutter/material.dart';

/// Palet warna resmi Vybrasi Specialty Roastery.
/// Mengusung estetika kedai sangrai kopi premium: Espresso, Crema Emas, Warm Cream, dan Latte Foam.
/// Lulus uji aksesibilitas kontras WCAG AA (>= 4.5:1).
class AppColors {
  AppColors._();

  // ── Specialty Roastery Palette ──────────────────────────────
  static const Color espresso          = Color(0xFF1E1510); // Deep Roasted Espresso
  static const Color espressoLight     = Color(0xFF2D2018); // Medium Dark Roast
  static const Color espressoBorder    = Color(0xFF423229); // Espresso Contour
  static const Color cremaAmber        = Color(0xFFC88A3A); // Golden Crema Accent
  static const Color cremaDark         = Color(0xFFA66E28); // Roasted Caramel
  static const Color cremaLight        = Color(0xFFE5B974); // Honey Crema
  static const Color latteFoam         = Color(0xFFF4EFEA); // Soft Steamed Milk Foam
  static const Color craftPaper        = Color(0xFFFBF9F5); // Warm Cream / Craft Background

  // ── Brand Accent (Golden Crema / Komisi) ─────────────────────
  static const Color goldPrimary       = cremaAmber;
  static const Color goldDark          = cremaDark;
  static const Color goldLight         = cremaLight;
  static const Color goldSurface       = latteFoam;
  static const Color goldAccessible    = Color(0xFF8F5D1B); // High-contrast WCAG 4.5:1

  // ── Brand Secondary (Forest Sage) ───────────────────────────
  static const Color green             = Color(0xFF2E6F40); // Sage Roastery Green
  static const Color greenDark         = Color(0xFF1E4D2B);
  static const Color greenSurface      = Color(0xFFEDF5EF);

  // ── Surface & Background ────────────────────────────────────
  static const Color scaffoldBg        = craftPaper;
  static const Color cardBg            = Colors.white;
  static const Color cardBorder        = Color(0xFFEDE7DF); // Warm Sand Border
  static const Color surfaceDark       = espresso;
  static const Color surfaceDarkAlt    = espressoLight;
  static const Color divider           = Color(0xFFEDE7DF);
  static const Color strokeLight       = Color(0xFFE6DFD5);
  static const Color strokeDark        = espressoBorder;

  // ── Tipografi ───────────────────────────────────────────────
  static const Color textPrimary       = espresso;
  static const Color textSecondary     = Color(0xFF5E4E44); // Warm Roasted Muted
  static const Color textMuted         = Color(0xFF96857B); // Light Roast Grey
  static const Color textOnDark        = Color(0xFFFAF7F2); // Warm Milk White
  static const Color textOnDarkMuted   = Color(0xFFB5A79E);

  // ── Status & Feedback ───────────────────────────────────────
  static const Color success           = Color(0xFF2E6F40); // Sage Roastery Green
  static const Color successSurface    = Color(0xFFEDF5EF);
  static const Color error             = Color(0xFFBC382D); // Terracotta Red
  static const Color errorSurface      = Color(0xFFFDF0EF);
  static const Color warning           = Color(0xFFC87A1E); // Cinnamon Ochre
  static const Color warningSurface    = Color(0xFFFDF5EB);
  static const Color info              = Color(0xFF256388); // Roast Indigo
  static const Color infoSurface       = Color(0xFFEEF5F9);
}