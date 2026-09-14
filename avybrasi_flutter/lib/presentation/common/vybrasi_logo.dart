import 'package:flutter/material.dart';
import '../../core/constants/app_colors.dart';

enum VybrasiLogoVariant {
  stacked,
  horizontal,
  iconOnly,
}

class VybrasiLogo extends StatelessWidget {
  final VybrasiLogoVariant variant;
  final double size;
  final bool dark;

  const VybrasiLogo({
    super.key,
    this.variant = VybrasiLogoVariant.stacked,
    this.size = 56,
    this.dark = false,
  });

  @override
  Widget build(BuildContext context) {
    switch (variant) {
      case VybrasiLogoVariant.iconOnly:
        return _buildEmblem(size);
      case VybrasiLogoVariant.horizontal:
        return Row(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            _buildEmblem(size),
            const SizedBox(width: 12),
            Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'VYBRASI',
                  style: TextStyle(
                    fontSize: size * 0.42,
                    fontWeight: FontWeight.w900,
                    letterSpacing: 2.2,
                    color: dark ? AppColors.textOnDark : AppColors.espresso,
                    height: 1.1,
                  ),
                ),
                Text(
                  'SPECIALTY ROASTERY',
                  style: TextStyle(
                    fontSize: size * 0.18,
                    fontWeight: FontWeight.w700,
                    letterSpacing: 1.8,
                    color: AppColors.cremaAmber,
                    height: 1.2,
                  ),
                ),
              ],
            ),
          ],
        );
      case VybrasiLogoVariant.stacked:
        return Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            _buildEmblem(size),
            SizedBox(height: size * 0.22),
            Text(
              'VYBRASI',
              style: TextStyle(
                fontSize: size * 0.38,
                fontWeight: FontWeight.w900,
                letterSpacing: 3.5,
                color: dark ? AppColors.textOnDark : AppColors.espresso,
              ),
            ),
            const SizedBox(height: 3),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
              decoration: BoxDecoration(
                color: AppColors.cremaAmber.withValues(alpha: 0.12),
                borderRadius: BorderRadius.circular(4),
              ),
              child: const Text(
                'SPECIALTY COFFEE ROASTERY',
                style: TextStyle(
                  fontSize: 9,
                  fontWeight: FontWeight.w700,
                  letterSpacing: 1.8,
                  color: AppColors.cremaDark,
                ),
              ),
            ),
          ],
        );
    }
  }

  Widget _buildEmblem(double s) {
    return Container(
      width: s,
      height: s,
      decoration: BoxDecoration(
        color: AppColors.espresso,
        shape: BoxShape.circle,
        border: Border.all(color: AppColors.cremaAmber, width: s * 0.035),
        boxShadow: [
          BoxShadow(
            color: AppColors.espresso.withValues(alpha: 0.18),
            blurRadius: s * 0.25,
            offset: Offset(0, s * 0.08),
          ),
        ],
      ),
      child: Stack(
        alignment: Alignment.center,
        children: [
          // Background coffee aroma ring
          Container(
            width: s * 0.76,
            height: s * 0.76,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              border: Border.all(
                color: AppColors.cremaAmber.withValues(alpha: 0.25),
                width: 1,
              ),
            ),
          ),
          // Coffee roastery bean & steam emblem
          Icon(
            Icons.coffee_rounded,
            size: s * 0.52,
            color: AppColors.cremaAmber,
          ),
        ],
      ),
    );
  }
}

