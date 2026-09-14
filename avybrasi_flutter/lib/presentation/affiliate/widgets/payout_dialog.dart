import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/utils/currency_formatter.dart';
import '../../../core/services/supabase_service.dart';

class PayoutDialog extends StatefulWidget {
  final String                idAffiliate;
  final double                currentSaldo;
  final double                minimumPayout;
  final Map<String, dynamic>? paymentMethod;
  final VoidCallback          onSuccess;

  const PayoutDialog({
    super.key,
    required this.idAffiliate,
    required this.currentSaldo,
    required this.minimumPayout,
    this.paymentMethod,
    required this.onSuccess,
  });

  @override
  State<PayoutDialog> createState() => _PayoutDialogState();
}

class _PayoutDialogState extends State<PayoutDialog> {
  final _amountCtrl = TextEditingController();
  final _formKey    = GlobalKey<FormState>();
  bool  _isLoading  = false;

  @override
  void dispose() { _amountCtrl.dispose(); super.dispose(); }

  double get _parsedAmount =>
      double.tryParse(_amountCtrl.text.replaceAll('.', '').trim()) ?? 0.0;

  Future<void> _submitPayout() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() => _isLoading = true);

    final success = await SupabaseService.insertPayoutRequest(
      idAffiliate: widget.idAffiliate,
      jumlah: _parsedAmount,
    );

    if (!mounted) return;
    final messenger = ScaffoldMessenger.of(context);
    final amount    = _parsedAmount;
    setState(() => _isLoading = false);
    Navigator.pop(context);

    if (success) {
      widget.onSuccess();
      messenger.showSnackBar(SnackBar(
        content: Text('Pengajuan ${CurrencyFormatter.formatRupiah(amount)} berhasil!'),
        backgroundColor: AppColors.success,
        behavior: SnackBarBehavior.floating,
      ));
    } else {
      messenger.showSnackBar(const SnackBar(
        content: Text('Gagal mengajukan pencairan. Silakan coba lagi.'),
        backgroundColor: AppColors.error,
        behavior: SnackBarBehavior.floating,
      ));
    }
  }

  @override
  Widget build(BuildContext context) {
    final bank     = widget.paymentMethod?['bank']?.toString()           ?? '';
    final rek      = widget.paymentMethod?['nomor_rekening']?.toString() ?? '';
    final atasNama = widget.paymentMethod?['atas_nama']?.toString()      ?? '';
    final hasPay   = bank.isNotEmpty && rek.isNotEmpty;

    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      backgroundColor: Colors.white,
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Form(
          key: _formKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('Cairkan Komisi', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: AppColors.textPrimary)),
              const SizedBox(height: 8),
              Text('Saldo Tersedia: ${CurrencyFormatter.formatRupiah(widget.currentSaldo)}',
                  style: const TextStyle(fontSize: 14, fontWeight: FontWeight.bold, color: AppColors.goldAccessible)),
              const SizedBox(height: 4),
              Text('Minimal Penarikan: ${CurrencyFormatter.formatRupiah(widget.minimumPayout)}',
                  style: const TextStyle(fontSize: 12, color: AppColors.textSecondary)),
              const SizedBox(height: 14),
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: AppColors.scaffoldBg,
                  borderRadius: BorderRadius.circular(10),
                  border: Border.all(color: AppColors.divider),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('Rekening Tujuan Transfer:', style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: AppColors.textMuted)),
                    const SizedBox(height: 4),
                    Text(
                      hasPay ? '$bank - $rek' : 'Belum diatur',
                      style: TextStyle(fontSize: 13, fontWeight: FontWeight.bold, color: hasPay ? AppColors.textPrimary : AppColors.error),
                    ),
                    if (hasPay && atasNama.isNotEmpty) ...[
                      const SizedBox(height: 2),
                      Text('a/n $atasNama', style: const TextStyle(fontSize: 12, color: AppColors.textSecondary)),
                    ],
                    if (!hasPay) ...[
                      const SizedBox(height: 6),
                      const Text('Harap atur rekening di tab Profil sebelum mencairkan.',
                          style: TextStyle(fontSize: 11, color: AppColors.error)),
                    ],
                  ],
                ),
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _amountCtrl,
                keyboardType: TextInputType.number,
                inputFormatters: [FilteringTextInputFormatter.digitsOnly],
                decoration: const InputDecoration(
                  labelText: 'Nominal Penarikan',
                  prefixText: 'Rp  ',
                  hintText: 'Contoh: 100000',
                ),
                validator: (value) {
                  final amount = double.tryParse((value ?? '').replaceAll('.', '').trim());
                  if (amount == null || amount <= 0) return 'Masukkan jumlah penarikan yang valid';
                  if (amount < widget.minimumPayout) {
                    return 'Minimal ${CurrencyFormatter.formatRupiah(widget.minimumPayout)}';
                  }
                  if (amount > widget.currentSaldo) return 'Melebihi saldo tersedia';
                  return null;
                },
              ),
              const SizedBox(height: 20),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton(
                      onPressed: _isLoading ? null : () => Navigator.pop(context),
                      child: const Text('Batal'),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: ElevatedButton(
                      onPressed: (!hasPay || _isLoading) ? null : _submitPayout,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.espresso,
                        foregroundColor: AppColors.textOnDark,
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                      ),
                      child: _isLoading
                          ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2, color: AppColors.cremaAmber))
                          : const Text('Ajukan Penarikan', style: TextStyle(fontWeight: FontWeight.w700, fontSize: 13)),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}