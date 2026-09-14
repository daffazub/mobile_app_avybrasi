import 'package:flutter/material.dart';
import '../../core/constants/app_colors.dart';

/// Memastikan antarmuka mobile tetap memiliki ukuran proporsional di layar lebar (Web / Desktop),
/// dengan batas maksimal 500px dan frame bayangan lembut, serta 100% fleksibel di layar smartphone.
class ResponsiveContainer extends StatelessWidget {
  final Widget child;
  final Color? backgroundColor;
  final double maxWidth;

  const ResponsiveContainer({
    super.key,
    required this.child,
    this.backgroundColor,
    this.maxWidth = 480,
  });

  @override
  Widget build(BuildContext context) {
    final screenWidth = MediaQuery.of(context).size.width;
    final isDesktop = screenWidth > maxWidth + 40;

    if (!isDesktop) {
      return Container(
        color: backgroundColor ?? AppColors.scaffoldBg,
        child: child,
      );
    }

    return Container(
      color: const Color(0xFFF0EBE3), // Elegant desktop ambient background
      alignment: Alignment.center,
      child: Container(
        constraints: BoxConstraints(maxWidth: maxWidth),
        decoration: BoxDecoration(
          color: backgroundColor ?? AppColors.scaffoldBg,
          boxShadow: [
            BoxShadow(
              color: AppColors.espresso.withValues(alpha: 0.08),
              blurRadius: 32,
              offset: const Offset(0, 8),
            ),
          ],
        ),
        child: child,
      ),
    );
  }
}

