import 'dart:async';
import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import '../../core/constants/app_colors.dart';
import '../../core/services/supabase_service.dart';
import '../affiliate/affiliate_dashboard_screen.dart';
import 'login_screen.dart';

class RoleGatekeeper extends StatefulWidget {
  const RoleGatekeeper({super.key});
  @override
  State<RoleGatekeeper> createState() => _RoleGatekeeperState();
}

class _RoleGatekeeperState extends State<RoleGatekeeper> {
  StreamSubscription<AuthState>? _authSub;
  bool _checkingRole = true;
  String? _userRole;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    if (SupabaseService.currentUser != null) {
      _resolveUserRole();
    } else {
      setState(() => _checkingRole = false);
    }
    _authSub = SupabaseService.authStateChanges.listen((data) {
      if (!mounted) return;
      if (data.session == null) {
        setState(() {
          _userRole = null;
          _checkingRole = false;
          _errorMessage = null;
        });
      } else {
        _resolveUserRole();
      }
    });
  }

  @override
  void dispose() {
    _authSub?.cancel();
    super.dispose();
  }

  Future<void> _resolveUserRole() async {
    if (!mounted) return;
    setState(() { _checkingRole = true; _errorMessage = null; });
    try {
      final role = await SupabaseService.resolveUserRole();
      if (mounted) setState(() { _userRole = role; _checkingRole = false; });
    } catch (e) {
      if (mounted) setState(() { _errorMessage = e.toString(); _checkingRole = false; });
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_checkingRole) {
      return const Scaffold(
        backgroundColor: Colors.white,
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              CircularProgressIndicator(color: AppColors.cremaAmber),
              SizedBox(height: 16),
              Text('Memeriksa akun...', style: TextStyle(color: AppColors.textSecondary, fontSize: 13)),
            ],
          ),
        ),
      );
    }
    if (SupabaseService.currentUser == null) return const LoginScreen();
    if (_errorMessage != null) {
      return Scaffold(
        body: SafeArea(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(Icons.error_outline_rounded, color: AppColors.error, size: 56),
                const SizedBox(height: 16),
                const Text('Terjadi Kesalahan', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                const SizedBox(height: 8),
                Text(_errorMessage!, textAlign: TextAlign.center, style: const TextStyle(color: AppColors.textSecondary, fontSize: 13)),
                const SizedBox(height: 24),
                ElevatedButton.icon(
                  onPressed: _resolveUserRole,
                  icon: const Icon(Icons.refresh_rounded),
                  label: const Text('Coba Lagi'),
                  style: ElevatedButton.styleFrom(backgroundColor: AppColors.espresso, foregroundColor: Colors.white),
                ),
                const SizedBox(height: 12),
                TextButton(onPressed: SupabaseService.signOut, child: const Text('Keluar', style: TextStyle(color: AppColors.error))),
              ],
            ),
          ),
        ),
      );
    }
    if (_userRole == 'affiliate') return const AffiliateDashboardScreen();
    return Scaffold(
      backgroundColor: Colors.white,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(color: AppColors.warning.withValues(alpha: 0.12), shape: BoxShape.circle),
                child: const Icon(Icons.coffee_rounded, size: 48, color: AppColors.cremaDark),
              ),
              const SizedBox(height: 20),
              const Text('Akses Khusus Mitra', style: TextStyle(fontSize: 20, fontWeight: FontWeight.w800, color: AppColors.textPrimary)),
              const SizedBox(height: 10),
              Text(
                'Akun Anda terdaftar dengan role: $_userRole. Portal ini khusus untuk Mitra Affiliate Vybrasi Roastery.',
                textAlign: TextAlign.center,
                style: const TextStyle(fontSize: 13, color: AppColors.textSecondary, height: 1.45),
              ),
              const SizedBox(height: 32),
              SizedBox(
                width: double.infinity, height: 48,
                child: OutlinedButton.icon(
                  onPressed: SupabaseService.signOut,
                  icon: const Icon(Icons.logout_rounded, color: AppColors.error),
                  label: const Text('Keluar & Ganti Akun', style: TextStyle(color: AppColors.error, fontWeight: FontWeight.bold)),
                  style: OutlinedButton.styleFrom(side: const BorderSide(color: AppColors.error), shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12))),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}