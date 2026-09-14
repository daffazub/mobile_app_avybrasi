import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/utils/currency_formatter.dart';
import '../../../data/models/affiliate_model.dart';
import '../../../data/models/profile_model.dart';
import '../../../data/models/transaksi_model.dart';

class AffHomeTab extends StatefulWidget {
  final ProfileModel?        profile;
  final AffiliateModel?      affiliate;
  final List<TransaksiModel> transaksiList;
  final bool                 isLoading;
  final Future<void> Function() onRefresh;
  final VoidCallback         onWithdrawTap;

  const AffHomeTab({
    super.key,
    required this.profile,
    required this.affiliate,
    required this.transaksiList,
    required this.isLoading,
    required this.onRefresh,
    required this.onWithdrawTap,
  });

  @override
  State<AffHomeTab> createState() => _AffHomeTabState();
}

class _AffHomeTabState extends State<AffHomeTab> {
  bool _isCopied = false;

  void _copyCode(String code) async {
    await Clipboard.setData(ClipboardData(text: code));
    if (!mounted) return;
    setState(() => _isCopied = true);
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Row(
          children: [
            const Icon(Icons.check_circle_rounded, color: Colors.white, size: 18),
            const SizedBox(width: 8),
            Text('Kode referral "$code" berhasil disalin!'),
          ],
        ),
        backgroundColor: AppColors.success,
        behavior: SnackBarBehavior.floating,
        duration: const Duration(seconds: 2),
      ),
    );
    Future.delayed(const Duration(seconds: 2), () {
      if (mounted) setState(() => _isCopied = false);
    });
  }

  @override
  Widget build(BuildContext context) {
    if (widget.isLoading) return _buildSkeleton();

    final aff       = widget.affiliate;
    final profile   = widget.profile;
    final nama      = aff?.namaLengkap ?? profile?.fullName ?? profile?.username ?? 'Mitra Roastery';
    final isActive  = aff?.isActive ?? true;
    final canPayout = (aff?.totalKomisi ?? 0) >= (aff?.minimumPayout ?? 100000);

    final totalSukses = widget.transaksiList.where((t) => t.isPaid).length;
    final totalOmset  = widget.transaksiList.where((t) => t.isPaid).fold(0.0, (sum, t) => sum + t.totalHarga);

    return RefreshIndicator(
      color: AppColors.cremaAmber,
      backgroundColor: Colors.white,
      onRefresh: widget.onRefresh,
      child: ListView(
        padding: EdgeInsets.zero,
        physics: const AlwaysScrollableScrollPhysics(parent: BouncingScrollPhysics()),
        children: [
          // ── Roastery Partner Welcome Card ─────────────────────
          Container(
            padding: const EdgeInsets.fromLTRB(20, 20, 20, 24),
            decoration: BoxDecoration(
              color: AppColors.espresso,
              borderRadius: const BorderRadius.vertical(bottom: Radius.circular(24)),
              boxShadow: [
                BoxShadow(
                  color: AppColors.espresso.withValues(alpha: 0.12),
                  blurRadius: 16,
                  offset: const Offset(0, 6),
                ),
              ],
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              const Icon(Icons.local_cafe_outlined, size: 14, color: AppColors.cremaAmber),
                              const SizedBox(width: 6),
                              Text(
                                'Selamat Datang, Mitra Roastery',
                                style: TextStyle(
                                  color: AppColors.textOnDarkMuted,
                                  fontSize: 12,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 4),
                          Text(
                            nama,
                            style: const TextStyle(
                              color: AppColors.textOnDark,
                              fontSize: 20,
                              fontWeight: FontWeight.w800,
                              letterSpacing: -0.3,
                            ),
                          ),
                        ],
                      ),
                    ),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                      decoration: BoxDecoration(
                        color: isActive
                            ? AppColors.green.withValues(alpha: 0.18)
                            : AppColors.error.withValues(alpha: 0.18),
                        borderRadius: BorderRadius.circular(20),
                        border: Border.all(
                          color: isActive ? AppColors.green : AppColors.error,
                          width: 1,
                        ),
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          CircleAvatar(
                            radius: 3.5,
                            backgroundColor: isActive ? AppColors.green : AppColors.error,
                          ),
                          const SizedBox(width: 5),
                          Text(
                            isActive ? 'AKTIF' : aff?.statusAffiliate.toUpperCase() ?? 'SUSPENDED',
                            style: TextStyle(
                              fontSize: 10,
                              fontWeight: FontWeight.w800,
                              letterSpacing: 0.5,
                              color: isActive ? const Color(0xFF5DD386) : const Color(0xFFFF8B80),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 20),

                // ── Referral Code Banner ────────────────────────────
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
                  decoration: BoxDecoration(
                    color: AppColors.surfaceDarkAlt,
                    borderRadius: BorderRadius.circular(16),
                    border: Border.all(color: AppColors.strokeDark, width: 1),
                  ),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text(
                            'KODE REFERRAL KOPI ANDA:',
                            style: TextStyle(
                              color: AppColors.textOnDarkMuted,
                              fontSize: 10,
                              fontWeight: FontWeight.w700,
                              letterSpacing: 1.2,
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            aff?.kodeReferal ?? '-',
                            style: const TextStyle(
                              color: AppColors.cremaAmber,
                              fontSize: 19,
                              fontWeight: FontWeight.w900,
                              letterSpacing: 2.0,
                            ),
                          ),
                        ],
                      ),
                      ElevatedButton.icon(
                        onPressed: () => _copyCode(aff?.kodeReferal ?? ''),
                        icon: Icon(
                          _isCopied ? Icons.check_circle_rounded : Icons.copy_rounded,
                          size: 15,
                          color: AppColors.espresso,
                        ),
                        label: Text(_isCopied ? 'Tersalin!' : 'Salin'),
                        style: ElevatedButton.styleFrom(
                          backgroundColor: AppColors.cremaAmber,
                          foregroundColor: AppColors.espresso,
                          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                          minimumSize: Size.zero,
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                          textStyle: const TextStyle(fontWeight: FontWeight.w800, fontSize: 13),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),

          // ── Main Content Cards ────────────────────────────────
          Padding(
            padding: const EdgeInsets.all(18),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // ── Card Saldo Komisi ─────────────────────────────
                Container(
                  padding: const EdgeInsets.all(20),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(18),
                    border: Border.all(color: AppColors.cardBorder),
                    boxShadow: [
                      BoxShadow(
                        color: AppColors.espresso.withValues(alpha: 0.03),
                        blurRadius: 14,
                        offset: const Offset(0, 4),
                      ),
                    ],
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          const Text(
                            'Saldo Komisi Siap Ditarik',
                            style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600, color: AppColors.textSecondary),
                          ),
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                            decoration: BoxDecoration(
                              color: AppColors.latteFoam,
                              borderRadius: BorderRadius.circular(6),
                            ),
                            child: Text(
                              '${aff?.komisiPersen.toStringAsFixed(0) ?? '5'}% Komisi Roastery',
                              style: const TextStyle(fontSize: 11, fontWeight: FontWeight.w700, color: AppColors.goldAccessible),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 8),
                      Text(
                        CurrencyFormatter.formatRupiah(aff?.totalKomisi ?? 0),
                        style: const TextStyle(
                          fontSize: 28,
                          fontWeight: FontWeight.w900,
                          color: AppColors.espresso,
                          letterSpacing: -0.5,
                        ),
                      ),
                      const SizedBox(height: 14),
                      const Divider(height: 1),
                      const SizedBox(height: 14),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              const Text('Batas Min. Tarik:', style: TextStyle(fontSize: 11, color: AppColors.textMuted)),
                              const SizedBox(height: 2),
                              Text(
                                CurrencyFormatter.formatRupiah(aff?.minimumPayout ?? 100000),
                                style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w700, color: AppColors.textPrimary),
                              ),
                            ],
                          ),
                          ElevatedButton(
                            onPressed: widget.onWithdrawTap,
                            style: ElevatedButton.styleFrom(
                              backgroundColor: canPayout ? AppColors.espresso : AppColors.latteFoam,
                              foregroundColor: canPayout ? AppColors.textOnDark : AppColors.textMuted,
                              padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 10),
                              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                              textStyle: const TextStyle(fontWeight: FontWeight.w700, fontSize: 13),
                            ),
                            child: Text(canPayout ? 'Cairkan Sekarang' : 'Lihat Pesanan'),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 16),

                // ── 2 Quick Metric Cards ──────────────────────────
                Row(
                  children: [
                    Expanded(
                      child: Container(
                        padding: const EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          color: Colors.white,
                          borderRadius: BorderRadius.circular(16),
                          border: Border.all(color: AppColors.cardBorder),
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Container(
                              padding: const EdgeInsets.all(8),
                              decoration: BoxDecoration(
                                color: AppColors.latteFoam,
                                borderRadius: BorderRadius.circular(10),
                              ),
                              child: const Icon(Icons.receipt_long_rounded, color: AppColors.cremaAmber, size: 22),
                            ),
                            const SizedBox(height: 12),
                            Text('$totalSukses', style: const TextStyle(fontSize: 22, fontWeight: FontWeight.w800, color: AppColors.espresso)),
                            const SizedBox(height: 2),
                            const Text('Pesanan Kopi Sukses', style: TextStyle(fontSize: 11, color: AppColors.textSecondary, fontWeight: FontWeight.w500)),
                          ],
                        ),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Container(
                        padding: const EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          color: Colors.white,
                          borderRadius: BorderRadius.circular(16),
                          border: Border.all(color: AppColors.cardBorder),
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Container(
                              padding: const EdgeInsets.all(8),
                              decoration: BoxDecoration(
                                color: AppColors.greenSurface,
                                borderRadius: BorderRadius.circular(10),
                              ),
                              child: const Icon(Icons.trending_up_rounded, color: AppColors.green, size: 22),
                            ),
                            const SizedBox(height: 12),
                            Text(
                              CurrencyFormatter.formatRupiah(totalOmset),
                              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w800, color: AppColors.espresso),
                              overflow: TextOverflow.ellipsis,
                            ),
                            const SizedBox(height: 2),
                            const Text('Total Omset Referral', style: TextStyle(fontSize: 11, color: AppColors.textSecondary, fontWeight: FontWeight.w500)),
                          ],
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 24),

                // ── Recent Transactions Section ───────────────────
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      'Aktivitas Pesanan Terkini',
                      style: TextStyle(fontSize: 16, fontWeight: FontWeight.w800, color: AppColors.textPrimary),
                    ),
                    TextButton(
                      onPressed: widget.onWithdrawTap,
                      style: TextButton.styleFrom(foregroundColor: AppColors.cremaDark),
                      child: const Text('Lihat Semua', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w700)),
                    ),
                  ],
                ),
                const SizedBox(height: 8),

                if (widget.transaksiList.isEmpty)
                  Container(
                    width: double.infinity,
                    padding: const EdgeInsets.symmetric(vertical: 36, horizontal: 20),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(16),
                      border: Border.all(color: AppColors.cardBorder),
                    ),
                    child: Column(
                      children: [
                        Container(
                          padding: const EdgeInsets.all(16),
                          decoration: BoxDecoration(
                            color: AppColors.latteFoam,
                            shape: BoxShape.circle,
                          ),
                          child: const Icon(Icons.coffee_rounded, size: 36, color: AppColors.cremaAmber),
                        ),
                        const SizedBox(height: 14),
                        const Text(
                          'Belum Ada Pesanan Kopi',
                          style: TextStyle(fontSize: 15, fontWeight: FontWeight.w700, color: AppColors.textPrimary),
                        ),
                        const SizedBox(height: 4),
                        const Text(
                          'Bagikan kode referral Anda ke pecinta kopi untuk mulai mencetak komisi.',
                          textAlign: TextAlign.center,
                          style: TextStyle(fontSize: 12, color: AppColors.textSecondary, height: 1.4),
                        ),
                      ],
                    ),
                  )
                else
                  ...widget.transaksiList.take(3).map((trx) {
                    final komisi = trx.komisiAffiliate > 0 ? trx.komisiAffiliate : trx.totalHarga * 0.05;
                    return Container(
                      margin: const EdgeInsets.only(bottom: 10),
                      padding: const EdgeInsets.all(16),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(16),
                        border: Border.all(color: AppColors.cardBorder),
                      ),
                      child: Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.all(10),
                            decoration: BoxDecoration(
                              color: AppColors.latteFoam,
                              borderRadius: BorderRadius.circular(12),
                            ),
                            child: const Icon(Icons.shopping_bag_outlined, color: AppColors.cremaDark, size: 20),
                          ),
                          const SizedBox(width: 14),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  trx.noInvoice ?? 'Pesanan Kopi',
                                  style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 14, color: AppColors.textPrimary),
                                ),
                                const SizedBox(height: 2),
                                Text(
                                  CurrencyFormatter.formatTanggal(trx.createdAt),
                                  style: const TextStyle(fontSize: 11, color: AppColors.textMuted),
                                ),
                              ],
                            ),
                          ),
                          Column(
                            crossAxisAlignment: CrossAxisAlignment.end,
                            children: [
                              Text(
                                '+${CurrencyFormatter.formatRupiah(komisi)}',
                                style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 13, color: AppColors.goldAccessible),
                              ),
                              const SizedBox(height: 2),
                              Text(
                                CurrencyFormatter.formatRupiah(trx.totalHarga),
                                style: const TextStyle(fontSize: 11, color: AppColors.textMuted),
                              ),
                            ],
                          ),
                        ],
                      ),
                    );
                  }),
                const SizedBox(height: 20),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSkeleton() {
    return ListView(
      padding: const EdgeInsets.all(20),
      children: [
        Container(height: 170, decoration: BoxDecoration(color: AppColors.latteFoam, borderRadius: BorderRadius.circular(20))),
        const SizedBox(height: 20),
        Container(height: 140, decoration: BoxDecoration(color: AppColors.latteFoam, borderRadius: BorderRadius.circular(16))),
        const SizedBox(height: 16),
        Row(
          children: [
            Expanded(child: Container(height: 100, decoration: BoxDecoration(color: AppColors.latteFoam, borderRadius: BorderRadius.circular(14)))),
            const SizedBox(width: 12),
            Expanded(child: Container(height: 100, decoration: BoxDecoration(color: AppColors.latteFoam, borderRadius: BorderRadius.circular(14)))),
          ],
        ),
      ],
    );
  }
}