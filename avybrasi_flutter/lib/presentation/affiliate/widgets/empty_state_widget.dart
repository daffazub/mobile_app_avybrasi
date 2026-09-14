import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../../core/constants/app_colors.dart';

class EmptyStateWidget extends StatelessWidget {
  final String  title;
  final String  message;
  final String? referralCode;
  final IconData icon;

  const EmptyStateWidget({
    super.key,
    required this.title,
    required this.message,
    this.referralCode,
    this.icon = Icons.coffee_rounded,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: SingleChildScrollView(
        padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 48),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(22),
              decoration: BoxDecoration(
                color: AppColors.latteFoam,
                shape: BoxShape.circle,
                border: Border.all(color: AppColors.cremaAmber.withValues(alpha: 0.3), width: 1.5),
              ),
              child: Icon(icon, size: 48, color: AppColors.cremaDark),
            ),
            const SizedBox(height: 18),
            Text(
              title,
              textAlign: TextAlign.center,
              style: const TextStyle(fontSize: 17, fontWeight: FontWeight.w800, color: AppColors.textPrimary),
            ),
            const SizedBox(height: 8),
            Text(
              message,
              textAlign: TextAlign.center,
              style: const TextStyle(fontSize: 13, color: AppColors.textSecondary, height: 1.45),
            ),
            if (referralCode != null && referralCode!.isNotEmpty) ...[
              const SizedBox(height: 22),
              OutlinedButton.icon(
                onPressed: () {
                  Clipboard.setData(ClipboardData(text: referralCode!));
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(
                      content: Text('Kode referal "$referralCode" berhasil disalin!'),
                      behavior: SnackBarBehavior.floating,
                      backgroundColor: AppColors.espresso,
                    ),
                  );
                },
                icon: const Icon(Icons.copy_rounded, size: 16, color: AppColors.espresso),
                label: const Text(
                  'Salin Kode Referral',
                  style: TextStyle(color: AppColors.espresso, fontWeight: FontWeight.w700),
                ),
                style: OutlinedButton.styleFrom(
                  side: const BorderSide(color: AppColors.espresso, width: 1.2),
                  padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}