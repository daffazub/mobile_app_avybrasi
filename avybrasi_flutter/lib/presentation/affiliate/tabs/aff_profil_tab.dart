import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/utils/currency_formatter.dart';
import '../../../core/services/supabase_service.dart';
import '../../../data/models/affiliate_model.dart';
import '../../../data/models/payout_model.dart';
import '../../../data/models/profile_model.dart';

class AffProfilTab extends StatefulWidget {
  final ProfileModel?   profile;
  final AffiliateModel? affiliate;
  final List<PayoutModel> payoutList;
  final bool            isLoading;
  final VoidCallback    onRefresh;
  final VoidCallback    onLogout;

  const AffProfilTab({
    super.key,
    required this.profile,
    required this.affiliate,
    required this.payoutList,
    required this.isLoading,
    required this.onRefresh,
    required this.onLogout,
  });

  @override
  State<AffProfilTab> createState() => _AffProfilTabState();
}

class _AffProfilTabState extends State<AffProfilTab> {
  void _copyReferralCode(String code) async {
    await Clipboard.setData(ClipboardData(text: code));
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Kode referral "$code" disalin!'),
        backgroundColor: AppColors.espresso,
        duration: const Duration(seconds: 2),
      ),
    );
  }

  void _showEditPaymentDialog() {
    final affiliate = widget.affiliate;
    if (affiliate == null) return;

    final bankCtrl  = TextEditingController(text: affiliate.bank);
    final noRekCtrl = TextEditingController(text: affiliate.nomorRekening);
    final namaCtrl  = TextEditingController(text: affiliate.atasNama);

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (sheetCtx) {
        var isSaving = false;
        return StatefulBuilder(
          builder: (_, setInner) {
            return Padding(
              padding: EdgeInsets.only(bottom: MediaQuery.of(sheetCtx).viewInsets.bottom),
              child: Container(
                padding: const EdgeInsets.fromLTRB(24, 24, 24, 34),
                decoration: const BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
                ),
                child: SingleChildScrollView(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Center(
                        child: Container(
                          width: 40, height: 4,
                          decoration: BoxDecoration(color: AppColors.cardBorder, borderRadius: BorderRadius.circular(2)),
                        ),
                      ),
                      const SizedBox(height: 18),
                      const Text(
                        'Rekening Pencairan Komisi',
                        style: TextStyle(fontSize: 18, fontWeight: FontWeight.w800, color: AppColors.textPrimary),
                      ),
                      const SizedBox(height: 6),
                      const Text(
                        'Pastikan nama pemilik rekening sesuai data perbankan resmi Anda.',
                        style: TextStyle(fontSize: 12, color: AppColors.textSecondary),
                      ),
                      const SizedBox(height: 20),
                      TextFormField(
                        controller: bankCtrl,
                        decoration: const InputDecoration(
                          labelText: 'Nama Bank / E-Wallet',
                          hintText: 'BCA, Mandiri, GoPay, OVO',
                          prefixIcon: Icon(Icons.account_balance_rounded, size: 20, color: AppColors.textMuted),
                        ),
                      ),
                      const SizedBox(height: 14),
                      TextFormField(
                        controller: noRekCtrl,
                        keyboardType: TextInputType.number,
                        inputFormatters: [FilteringTextInputFormatter.digitsOnly],
                        decoration: const InputDecoration(
                          labelText: 'Nomor Rekening / No. HP',
                          hintText: '1234567890',
                          prefixIcon: Icon(Icons.numbers_rounded, size: 20, color: AppColors.textMuted),
                        ),
                      ),
                      const SizedBox(height: 14),
                      TextFormField(
                        controller: namaCtrl,
                        decoration: const InputDecoration(
                          labelText: 'Atas Nama Pemilik',
                          hintText: 'Nama lengkap sesuai rekening',
                          prefixIcon: Icon(Icons.person_outline_rounded, size: 20, color: AppColors.textMuted),
                        ),
                      ),
                      const SizedBox(height: 24),
                      SizedBox(
                        width: double.infinity, height: 50,
                        child: ElevatedButton(
                          onPressed: isSaving ? null : () async {
                            final b  = bankCtrl.text.trim();
                            final nr = noRekCtrl.text.trim();
                            final an = namaCtrl.text.trim();
                            if (b.isEmpty || nr.isEmpty || an.isEmpty) {
                              ScaffoldMessenger.of(sheetCtx).showSnackBar(
                                const SnackBar(content: Text('Semua field rekening wajib diisi!'), backgroundColor: AppColors.error),
                              );
                              return;
                            }
                            setInner(() => isSaving = true);
                            final messenger = ScaffoldMessenger.of(context);
                            final nav = Navigator.of(sheetCtx);
                            final success = await SupabaseService.updatePaymentMethod(
                              affiliateId: affiliate.idAffiliate,
                              bank: b,
                              nomorRekening: nr,
                              atasNama: an,
                            );
                            if (!mounted) return;
                            nav.pop();
                            if (success) {
                              widget.onRefresh();
                              messenger.showSnackBar(
                                const SnackBar(content: Text('Rekening pencairan berhasil diperbarui!'), backgroundColor: AppColors.success),
                              );
                            } else {
                              setInner(() => isSaving = false);
                              messenger.showSnackBar(
                                const SnackBar(content: Text('Gagal menyimpan rekening. Silakan coba lagi.'), backgroundColor: AppColors.error),
                              );
                            }
                          },
                          style: ElevatedButton.styleFrom(
                            backgroundColor: AppColors.espresso,
                            foregroundColor: AppColors.textOnDark,
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                          ),
                          child: isSaving
                              ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2, color: AppColors.cremaAmber))
                              : const Text('Simpan Rekening', style: TextStyle(fontWeight: FontWeight.w700)),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            );
          },
        );
      },
    );
  }

  void _showLogoutConfirmation() {
    showDialog(
      context: context,
      builder: (_) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
        title: const Text('Konfirmasi Keluar', style: TextStyle(fontWeight: FontWeight.w800)),
        content: const Text('Apakah Anda yakin ingin keluar dari akun mitra Vybrasi Roastery?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Batal', style: TextStyle(color: AppColors.textSecondary)),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.pop(context);
              widget.onLogout();
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.error,
              foregroundColor: Colors.white,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
            ),
            child: const Text('Keluar'),
          ),
        ],
      ),
    );
  }

  void _showBuktiTransferDialog(String url) {
    showDialog(
      context: context,
      builder: (_) => Dialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text('Bukti Transfer Admin', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 16)),
                  IconButton(
                    icon: const Icon(Icons.close_rounded, size: 20),
                    onPressed: () => Navigator.pop(context),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              ClipRRect(
                borderRadius: BorderRadius.circular(12),
                child: Image.network(
                  url,
                  fit: BoxFit.cover,
                  errorBuilder: (_, __, ___) => Container(
                    padding: const EdgeInsets.all(32),
                    color: AppColors.latteFoam,
                    child: const Column(
                      children: [
                        Icon(Icons.broken_image_rounded, size: 40, color: AppColors.textMuted),
                        SizedBox(height: 8),
                        Text('Gagal memuat gambar bukti transfer.', style: TextStyle(fontSize: 12, color: AppColors.textMuted)),
                      ],
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 16),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: () => Navigator.pop(context),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.espresso,
                    foregroundColor: Colors.white,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                  ),
                  child: const Text('Tutup'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    if (widget.isLoading) {
      return const Center(child: CircularProgressIndicator(color: AppColors.cremaAmber));
    }

    final profile   = widget.profile;
    final affiliate = widget.affiliate;
    final hasPaymentMethod = affiliate?.isPaymentMethodComplete ?? false;

    return RefreshIndicator(
      color: AppColors.cremaAmber,
      backgroundColor: Colors.white,
      onRefresh: () async => widget.onRefresh(),
      child: ListView(
        padding: const EdgeInsets.all(20),
        physics: const AlwaysScrollableScrollPhysics(parent: BouncingScrollPhysics()),
        children: [
          // ── Profile Card ──────────────────────────────────────
          Container(
            padding: const EdgeInsets.all(22),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(20),
              border: Border.all(color: AppColors.cardBorder),
              boxShadow: [
                BoxShadow(
                  color: AppColors.espresso.withValues(alpha: 0.02),
                  blurRadius: 14,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: Column(
              children: [
                Container(
                  padding: const EdgeInsets.all(4),
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    border: Border.all(color: AppColors.cremaAmber, width: 2),
                  ),
                  child: CircleAvatar(
                    radius: 36,
                    backgroundColor: AppColors.latteFoam,
                    backgroundImage: (profile?.avatarUrl != null && profile!.avatarUrl!.isNotEmpty)
                        ? NetworkImage(profile.avatarUrl!)
                        : null,
                    child: (profile?.avatarUrl == null || profile!.avatarUrl!.isEmpty)
                        ? const Icon(Icons.person_rounded, size: 40, color: AppColors.espresso)
                        : null,
                  ),
                ),
                const SizedBox(height: 12),
                Text(
                  affiliate?.namaLengkap ?? profile?.fullName ?? profile?.username ?? 'Mitra Roastery',
                  style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w800, color: AppColors.textPrimary),
                ),
                const SizedBox(height: 6),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                  decoration: BoxDecoration(
                    color: (affiliate?.isActive ?? false) ? AppColors.greenSurface : AppColors.errorSurface,
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Text(
                    (affiliate?.isActive ?? false) ? 'STATUS: MITRA AKTIF' : 'STATUS: SUSPENDED',
                    style: TextStyle(
                      fontSize: 10,
                      fontWeight: FontWeight.w800,
                      letterSpacing: 0.5,
                      color: (affiliate?.isActive ?? false) ? AppColors.green : AppColors.error,
                    ),
                  ),
                ),
                const SizedBox(height: 16),
                const Divider(),
                const SizedBox(height: 8),
                _infoRow(label: 'Username', value: profile?.username != null ? '@${profile!.username}' : '-', icon: Icons.alternate_email_rounded),
                _infoRow(label: 'No. Telepon', value: profile?.phone ?? '-', icon: Icons.phone_rounded),
                _infoRow(
                  label: 'Kode Referral', value: affiliate?.kodeReferal ?? '-', icon: Icons.qr_code_rounded,
                  trailing: affiliate != null
                      ? IconButton(
                          icon: const Icon(Icons.copy_rounded, size: 18, color: AppColors.cremaDark),
                          onPressed: () => _copyReferralCode(affiliate.kodeReferal),
                        )
                      : null,
                ),
                _infoRow(label: 'Bagi Hasil Komisi', value: '${affiliate?.komisiPersen.toStringAsFixed(0) ?? '5'}%', icon: Icons.percent_rounded),
              ],
            ),
          ),
          const SizedBox(height: 18),

          // ── Payout Method Card ────────────────────────────────
          Container(
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(20),
              border: Border.all(color: AppColors.cardBorder),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text('Rekening Pencairan Dana', style: TextStyle(fontSize: 15, fontWeight: FontWeight.w800, color: AppColors.textPrimary)),
                    TextButton.icon(
                      onPressed: _showEditPaymentDialog,
                      icon: const Icon(Icons.edit_rounded, size: 14, color: AppColors.cremaDark),
                      label: Text(
                        hasPaymentMethod ? 'Ubah' : 'Atur Sekarang',
                        style: const TextStyle(fontSize: 12, color: AppColors.cremaDark, fontWeight: FontWeight.w800),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                if (hasPaymentMethod) ...[
                  Container(
                    padding: const EdgeInsets.all(14),
                    decoration: BoxDecoration(
                      color: AppColors.latteFoam,
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(color: AppColors.cardBorder),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            const Icon(Icons.account_balance_rounded, size: 18, color: AppColors.espresso),
                            const SizedBox(width: 8),
                            Text(affiliate!.bank, style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 14, color: AppColors.espresso)),
                          ],
                        ),
                        const SizedBox(height: 6),
                        Text('No. Rek: ${affiliate.nomorRekening}', style: const TextStyle(fontSize: 13, color: AppColors.textSecondary, fontWeight: FontWeight.w600)),
                        const SizedBox(height: 2),
                        Text('A.n: ${affiliate.atasNama}', style: const TextStyle(fontSize: 12, color: AppColors.textMuted)),
                      ],
                    ),
                  ),
                ] else ...[
                  Container(
                    padding: const EdgeInsets.all(14),
                    decoration: BoxDecoration(
                      color: AppColors.warningSurface,
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(color: AppColors.warning.withValues(alpha: 0.3)),
                    ),
                    child: const Row(
                      children: [
                        Icon(Icons.warning_amber_rounded, color: AppColors.warning, size: 20),
                        SizedBox(width: 10),
                        Expanded(
                          child: Text(
                            'Rekening belum diatur. Atur rekening agar komisi dapat dicairkan.',
                            style: TextStyle(fontSize: 12, color: AppColors.warning, fontWeight: FontWeight.w600),
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ],
            ),
          ),
          const SizedBox(height: 18),

          // ── Payout Requests History ───────────────────────────
          const Text('Riwayat Penarikan Dana', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w800, color: AppColors.textPrimary)),
          const SizedBox(height: 10),
          if (widget.payoutList.isEmpty)
            Container(
              padding: const EdgeInsets.all(24),
              alignment: Alignment.center,
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: AppColors.cardBorder),
              ),
              child: const Text('Belum ada riwayat penarikan dana.', style: TextStyle(fontSize: 12, color: AppColors.textMuted)),
            )
          else
            ...widget.payoutList.map((payout) {
              final catatan = payout.keteranganAdmin;
              return Container(
                margin: const EdgeInsets.only(bottom: 10),
                padding: const EdgeInsets.all(14),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(14),
                  border: Border.all(color: AppColors.cardBorder),
                ),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(CurrencyFormatter.formatRupiah(payout.jumlah), style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 14, color: AppColors.textPrimary)),
                          const SizedBox(height: 2),
                          Text(CurrencyFormatter.formatDateTime(payout.createdAt), style: const TextStyle(fontSize: 11, color: AppColors.textMuted)),
                          if (catatan != null && catatan.isNotEmpty) ...[
                            const SizedBox(height: 4),
                            Text('Catatan: $catatan', style: const TextStyle(fontSize: 11, color: AppColors.textSecondary, fontStyle: FontStyle.italic)),
                          ],
                          if (payout.buktiTransferUrl != null && payout.buktiTransferUrl!.isNotEmpty) ...[
                            const SizedBox(height: 8),
                            InkWell(
                              onTap: () => _showBuktiTransferDialog(payout.buktiTransferUrl!),
                              borderRadius: BorderRadius.circular(6),
                              child: Padding(
                                padding: const EdgeInsets.symmetric(vertical: 2),
                                child: Row(
                                  mainAxisSize: MainAxisSize.min,
                                  children: const [
                                    Icon(Icons.receipt_rounded, size: 14, color: AppColors.cremaDark),
                                    SizedBox(width: 5),
                                    Text(
                                      'Lihat Bukti Transfer Admin',
                                      style: TextStyle(
                                        fontSize: 11,
                                        fontWeight: FontWeight.w700,
                                        color: AppColors.cremaDark,
                                        decoration: TextDecoration.underline,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                            ),
                          ],
                        ],
                      ),
                    ),
                    const SizedBox(width: 8),
                    _payoutBadge(payout.status),
                  ],
                ),
              );
            }),
          const SizedBox(height: 28),

          // ── Logout Button ─────────────────────────────────────
          SizedBox(
            width: double.infinity, height: 50,
            child: OutlinedButton.icon(
              onPressed: _showLogoutConfirmation,
              icon: const Icon(Icons.logout_rounded, color: AppColors.error, size: 18),
              label: const Text('Keluar dari Akun Mitra', style: TextStyle(color: AppColors.error, fontWeight: FontWeight.w700)),
              style: OutlinedButton.styleFrom(
                side: const BorderSide(color: AppColors.error),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              ),
            ),
          ),
          const SizedBox(height: 24),
        ],
      ),
    );
  }

  Widget _infoRow({required String label, required String value, required IconData icon, Widget? trailing}) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 7),
      child: Row(
        children: [
          Icon(icon, size: 18, color: AppColors.textMuted),
          const SizedBox(width: 10),
          Expanded(child: Text(label, style: const TextStyle(fontSize: 13, color: AppColors.textSecondary))),
          Text(value, style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w700, color: AppColors.textPrimary)),
          if (trailing != null) trailing,
        ],
      ),
    );
  }

  Widget _payoutBadge(String status) {
    Color bg;
    Color fg;
    String label;
    switch (status.toLowerCase()) {
      case 'approved':
      case 'paid':
        bg = AppColors.greenSurface;
        fg = AppColors.green;
        label = 'Berhasil';
        break;
      case 'rejected':
        bg = AppColors.errorSurface;
        fg = AppColors.error;
        label = 'Ditolak';
        break;
      default:
        bg = AppColors.warningSurface;
        fg = AppColors.warning;
        label = 'Diproses';
    }
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 4),
      decoration: BoxDecoration(color: bg, borderRadius: BorderRadius.circular(6)),
      child: Text(label, style: TextStyle(fontSize: 11, fontWeight: FontWeight.w800, color: fg)),
    );
  }
}