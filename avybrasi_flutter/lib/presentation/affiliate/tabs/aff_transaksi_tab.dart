import 'package:flutter/material.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/utils/currency_formatter.dart';
import '../../../data/models/affiliate_model.dart';
import '../../../data/models/transaksi_model.dart';
import '../widgets/empty_state_widget.dart';
import '../widgets/payout_dialog.dart';

class AffTransaksiTab extends StatefulWidget {
  final List<TransaksiModel> transaksiList;
  final AffiliateModel?      affiliate;
  final bool                 isLoading;
  final bool                 hasPendingPayout;
  final Future<void> Function() onRefresh;
  final VoidCallback         onWithdrawSuccess;

  const AffTransaksiTab({
    super.key,
    required this.transaksiList,
    required this.affiliate,
    required this.isLoading,
    required this.hasPendingPayout,
    required this.onRefresh,
    required this.onWithdrawSuccess,
  });

  @override
  State<AffTransaksiTab> createState() => _AffTransaksiTabState();
}

class _AffTransaksiTabState extends State<AffTransaksiTab> {
  String _activeFilter = 'Semua';

  List<TransaksiModel> get _filtered {
    if (_activeFilter == 'Semua') return widget.transaksiList;
    if (_activeFilter == 'Sukses') return widget.transaksiList.where((t) => t.isPaid).toList();
    if (_activeFilter == 'Pending') return widget.transaksiList.where((t) => t.isPending).toList();
    return widget.transaksiList;
  }

  void _showPayoutDialog() {
    final aff = widget.affiliate;
    if (aff == null) return;
    showDialog(
      context: context,
      builder: (_) => PayoutDialog(
        idAffiliate: aff.idAffiliate,
        currentSaldo: aff.totalKomisi,
        minimumPayout: aff.minimumPayout,
        paymentMethod: aff.paymentMethod,
        onSuccess: widget.onWithdrawSuccess,
      ),
    );
  }

  Color _statusColor(String status) {
    switch (status.toLowerCase()) {
      case 'paid':
      case 'delivered':
        return AppColors.green;
      case 'pending':
      case 'processed':
      case 'shipped':
        return AppColors.warning;
      case 'cancelled':
      case 'refunded':
        return AppColors.error;
      default:
        return AppColors.textSecondary;
    }
  }

  String _statusLabel(String status) {
    switch (status.toLowerCase()) {
      case 'paid':      return 'Selesai';
      case 'delivered': return 'Terkirim';
      case 'pending':   return 'Menunggu';
      case 'processed': return 'Diproses';
      case 'shipped':   return 'Dikirim';
      case 'cancelled': return 'Dibatalkan';
      case 'refunded':  return 'Dikembalikan';
      default:          return status;
    }
  }

  @override
  Widget build(BuildContext context) {
    final saldo          = widget.affiliate?.totalKomisi  ?? 0;
    final minPayout      = widget.affiliate?.minimumPayout ?? 100000;
    final canWithdraw    = saldo >= minPayout && !widget.hasPendingPayout;

    return RefreshIndicator(
      color: AppColors.cremaAmber,
      backgroundColor: Colors.white,
      onRefresh: widget.onRefresh,
      child: Column(
        children: [
          // ── Header & Filter Bar ───────────────────────────────
          Container(
            color: Colors.white,
            padding: const EdgeInsets.fromLTRB(20, 16, 20, 16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Riwayat Pesanan Roastery',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.w800, color: AppColors.textPrimary, letterSpacing: -0.3),
                ),
                const SizedBox(height: 4),
                const Text(
                  'Daftar pesanan biji kopi yang menggunakan kode referral Anda',
                  style: TextStyle(fontSize: 12, color: AppColors.textSecondary),
                ),
                const SizedBox(height: 14),
                SingleChildScrollView(
                  scrollDirection: Axis.horizontal,
                  physics: const BouncingScrollPhysics(),
                  child: Row(
                    children: ['Semua', 'Sukses', 'Pending'].map((f) {
                      final isSelected = _activeFilter == f;
                      return Padding(
                        padding: const EdgeInsets.only(right: 8),
                        child: FilterChip(
                          label: Text(f),
                          selected: isSelected,
                          onSelected: (_) => setState(() => _activeFilter = f),
                          backgroundColor: AppColors.latteFoam,
                          selectedColor: AppColors.espresso,
                          checkmarkColor: AppColors.cremaAmber,
                          labelStyle: TextStyle(
                            color: isSelected ? AppColors.textOnDark : AppColors.textSecondary,
                            fontWeight: isSelected ? FontWeight.w700 : FontWeight.w500,
                            fontSize: 12,
                          ),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(20),
                            side: BorderSide(
                              color: isSelected ? AppColors.espresso : AppColors.cardBorder,
                              width: 1,
                            ),
                          ),
                          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                        ),
                      );
                    }).toList(),
                  ),
                ),
              ],
            ),
          ),
          const Divider(height: 1, thickness: 1, color: AppColors.cardBorder),

          // ── Transaction List ──────────────────────────────────
          Expanded(
            child: widget.isLoading
                ? const Center(child: CircularProgressIndicator(color: AppColors.cremaAmber))
                : _filtered.isEmpty
                    ? EmptyStateWidget(
                        icon: Icons.receipt_long_outlined,
                        title: 'Belum Ada Pesanan',
                        message: 'Bagikan kode referral roastery Anda ke pembeli kopi untuk mulai meraih komisi.',
                        referralCode: widget.affiliate?.kodeReferal,
                      )
                    : ListView.builder(
                        padding: const EdgeInsets.all(16),
                        physics: const AlwaysScrollableScrollPhysics(parent: BouncingScrollPhysics()),
                        itemCount: _filtered.length,
                        itemBuilder: (_, i) {
                          final trx    = _filtered[i];
                          final komisi = trx.komisiAffiliate > 0
                              ? trx.komisiAffiliate
                              : trx.totalHarga * 0.05;
                          final statusColor = _statusColor(trx.status);

                          return Container(
                            margin: const EdgeInsets.only(bottom: 12),
                            padding: const EdgeInsets.all(16),
                            decoration: BoxDecoration(
                              color: Colors.white,
                              borderRadius: BorderRadius.circular(16),
                              border: Border.all(color: AppColors.cardBorder),
                              boxShadow: [
                                BoxShadow(
                                  color: AppColors.espresso.withValues(alpha: 0.02),
                                  blurRadius: 10,
                                  offset: const Offset(0, 3),
                                ),
                              ],
                            ),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(
                                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                  children: [
                                    Flexible(
                                      child: Text(
                                        trx.noInvoice ?? 'INV-VYB-${(trx.idTransaksi ?? '').length >= 6 ? trx.idTransaksi!.substring(0, 6).toUpperCase() : (trx.idTransaksi ?? '000000').toUpperCase()}',
                                        style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w700, color: AppColors.textPrimary),
                                        overflow: TextOverflow.ellipsis,
                                      ),
                                    ),
                                    const SizedBox(width: 8),
                                    Container(
                                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                      decoration: BoxDecoration(
                                        color: statusColor.withValues(alpha: 0.12),
                                        borderRadius: BorderRadius.circular(6),
                                      ),
                                      child: Text(
                                        _statusLabel(trx.status),
                                        style: TextStyle(fontSize: 11, fontWeight: FontWeight.w800, color: statusColor),
                                      ),
                                    ),
                                  ],
                                ),
                                const SizedBox(height: 6),
                                Row(
                                  children: [
                                    const Icon(Icons.access_time_rounded, size: 12, color: AppColors.textMuted),
                                    const SizedBox(width: 4),
                                    Text(
                                      CurrencyFormatter.formatTanggal(trx.createdAt),
                                      style: const TextStyle(fontSize: 11, color: AppColors.textMuted),
                                    ),
                                  ],
                                ),
                                const SizedBox(height: 12),
                                const Divider(height: 1),
                                const SizedBox(height: 12),
                                Row(
                                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                  children: [
                                    Column(
                                      crossAxisAlignment: CrossAxisAlignment.start,
                                      children: [
                                        const Text('Total Belanja Kopi', style: TextStyle(fontSize: 10, color: AppColors.textMuted)),
                                        Text(
                                          CurrencyFormatter.formatRupiah(trx.totalHarga),
                                          style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w700, color: AppColors.textPrimary),
                                        ),
                                      ],
                                    ),
                                    Column(
                                      crossAxisAlignment: CrossAxisAlignment.end,
                                      children: [
                                        const Text('Komisi Anda', style: TextStyle(fontSize: 10, color: AppColors.textMuted)),
                                        Text(
                                          CurrencyFormatter.formatRupiah(komisi),
                                          style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w800, color: AppColors.goldAccessible),
                                        ),
                                      ],
                                    ),
                                  ],
                                ),
                              ],
                            ),
                          );
                        },
                      ),
          ),

          // ── Bottom Payout CTA Bar ─────────────────────────────
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
            decoration: BoxDecoration(
              color: Colors.white,
              border: const Border(top: BorderSide(color: AppColors.cardBorder, width: 1)),
            ),
            child: Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Text('Saldo Siap Ditarik', style: TextStyle(fontSize: 11, color: AppColors.textMuted)),
                      Text(
                        CurrencyFormatter.formatRupiah(saldo),
                        style: const TextStyle(fontSize: 17, fontWeight: FontWeight.w900, color: AppColors.espresso),
                      ),
                    ],
                  ),
                ),
                ElevatedButton.icon(
                  onPressed: canWithdraw ? _showPayoutDialog : null,
                  icon: const Icon(Icons.arrow_upward_rounded, size: 16),
                  label: const Text('Tarik Komisi'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: canWithdraw ? AppColors.espresso : AppColors.latteFoam,
                    foregroundColor: canWithdraw ? AppColors.textOnDark : AppColors.textMuted,
                    padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                    textStyle: const TextStyle(fontWeight: FontWeight.w800, fontSize: 13),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}