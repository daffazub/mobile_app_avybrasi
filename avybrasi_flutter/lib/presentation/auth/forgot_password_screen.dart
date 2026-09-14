import 'package:flutter/material.dart';
import '../../core/constants/app_colors.dart';
import '../../core/services/supabase_service.dart';
import '../common/responsive_container.dart';
import '../common/vybrasi_logo.dart';

class ForgotPasswordScreen extends StatefulWidget {
  const ForgotPasswordScreen({super.key});
  @override
  State<ForgotPasswordScreen> createState() => _ForgotPasswordScreenState();
}

class _ForgotPasswordScreenState extends State<ForgotPasswordScreen> {
  final _formKey   = GlobalKey<FormState>();
  final _emailCtrl = TextEditingController();
  bool  _isLoading = false;
  bool  _isSuccess = false;
  String? _errorMessage;
  int   _cooldownSeconds = 0;

  @override
  void dispose() {
    _emailCtrl.dispose();
    super.dispose();
  }

  void _startCooldown() {
    setState(() => _cooldownSeconds = 60);
    Future.doWhile(() async {
      await Future.delayed(const Duration(seconds: 1));
      if (!mounted) return false;
      setState(() => _cooldownSeconds--);
      return _cooldownSeconds > 0;
    });
  }

  Future<void> _handleReset() async {
    if (_cooldownSeconds > 0) return;
    FocusScope.of(context).unfocus();
    if (!_formKey.currentState!.validate()) return;
    setState(() { _isLoading = true; _errorMessage = null; });
    try {
      await SupabaseService.resetPassword(email: _emailCtrl.text.trim());
      _startCooldown();
      if (mounted) setState(() { _isLoading = false; _isSuccess = true; });
    } catch (e) {
      if (mounted) {
        setState(() {
          _isLoading = false;
          _errorMessage = 'Gagal mengirim email. Periksa koneksi dan coba beberapa saat lagi.';
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.scaffoldBg,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        scrolledUnderElevation: 0,
        iconTheme: const IconThemeData(color: AppColors.espresso),
        title: const Text(
          'Reset Kata Sandi',
          style: TextStyle(color: AppColors.espresso, fontSize: 16, fontWeight: FontWeight.w700),
        ),
      ),
      body: ResponsiveContainer(
        child: SafeArea(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 20),
            child: _isSuccess ? _buildSuccess() : _buildForm(),
          ),
        ),
      ),
    );
  }

  Widget _buildForm() => Form(
    key: _formKey,
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        const SizedBox(height: 12),
        const VybrasiLogo(size: 48, variant: VybrasiLogoVariant.iconOnly),
        const SizedBox(height: 20),
        const Text(
          'Pemulihan Akun Mitra',
          style: TextStyle(fontSize: 22, fontWeight: FontWeight.w800, color: AppColors.textPrimary, letterSpacing: -0.3),
        ),
        const SizedBox(height: 8),
        const Text(
          'Masukkan email yang terdaftar di sistem Vybrasi. Kami akan mengirimkan tautan untuk mengatur ulang kata sandi Anda.',
          style: TextStyle(fontSize: 13, color: AppColors.textSecondary, height: 1.45),
        ),
        const SizedBox(height: 28),
        if (_errorMessage != null) ...[
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: AppColors.errorSurface,
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: AppColors.error.withValues(alpha: 0.25)),
            ),
            child: Text(_errorMessage!, style: const TextStyle(color: AppColors.error, fontSize: 12, fontWeight: FontWeight.w500)),
          ),
          const SizedBox(height: 18),
        ],
        TextFormField(
          controller: _emailCtrl,
          keyboardType: TextInputType.emailAddress,
          autofillHints: const [AutofillHints.email],
          decoration: const InputDecoration(
            labelText: 'Alamat Email Mitra',
            hintText: 'nama@domain.com',
            prefixIcon: Icon(Icons.email_outlined, size: 20, color: AppColors.textMuted),
          ),
          validator: (val) {
            if (val == null || val.trim().isEmpty) return 'Email wajib diisi';
            if (!RegExp(r'^[^@]+@[^@]+\.[^@]+').hasMatch(val.trim())) return 'Format email tidak valid';
            return null;
          },
        ),
        const SizedBox(height: 24),
        SizedBox(
          height: 52,
          child: ElevatedButton(
            onPressed: (_isLoading || _cooldownSeconds > 0) ? null : _handleReset,
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.espresso,
              foregroundColor: AppColors.textOnDark,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
            ),
            child: _isLoading
                ? const SizedBox(width: 22, height: 22, child: CircularProgressIndicator(strokeWidth: 2.2, color: AppColors.cremaAmber))
                : Text(
                    _cooldownSeconds > 0
                        ? 'Tunggu $_cooldownSeconds detik...'
                        : 'Kirim Tautan Reset',
                    style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 14),
                  ),
          ),
        ),
      ],
    ),
  );

  Widget _buildSuccess() => Column(
    mainAxisAlignment: MainAxisAlignment.center,
    crossAxisAlignment: CrossAxisAlignment.stretch,
    children: [
      Center(
        child: Container(
          padding: const EdgeInsets.all(22),
          decoration: BoxDecoration(
            color: AppColors.latteFoam,
            shape: BoxShape.circle,
            border: Border.all(color: AppColors.cremaAmber, width: 2),
          ),
          child: const Icon(Icons.mark_email_read_outlined, size: 48, color: AppColors.cremaDark),
        ),
      ),
      const SizedBox(height: 24),
      const Text(
        'Tautan Terkirim!',
        textAlign: TextAlign.center,
        style: TextStyle(fontSize: 22, fontWeight: FontWeight.w800, color: AppColors.textPrimary),
      ),
      const SizedBox(height: 10),
      Text(
        'Tautan pemulihan dikirim ke ${_emailCtrl.text.trim()}. Silakan periksa kotak masuk atau folder spam email Anda.',
        textAlign: TextAlign.center,
        style: const TextStyle(fontSize: 13, color: AppColors.textSecondary, height: 1.5),
      ),
      const SizedBox(height: 32),
      SizedBox(
        height: 50,
        child: OutlinedButton(
          onPressed: () => Navigator.pop(context),
          style: OutlinedButton.styleFrom(
            side: const BorderSide(color: AppColors.espresso),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
          ),
          child: const Text('Kembali ke Halaman Masuk', style: TextStyle(color: AppColors.espresso, fontWeight: FontWeight.w700)),
        ),
      ),
    ],
  );
}