import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/utils/currency_formatter.dart';
import '../../../data/models/affiliate_model.dart';
import '../../../data/models/transaksi_model.dart';

class AffKomisiTab extends StatefulWidget {
  final AffiliateModel?      affiliate;
  final List<TransaksiModel> transaksiList;
  final bool                 isLoading;
  final Future<void> Function() onRefresh;

  const AffKomisiTab({
    super.key,
    required this.affiliate,
    required this.transaksiList,
    required this.isLoading,
    required this.onRefresh,
  });

  @override
  State<AffKomisiTab> createState() => _AffKomisiTabState();
}

class _AffKomisiTabState extends State<AffKomisiTab> {
  int _selectedBarIndex = -1;

  @override
  Widget build(BuildContext context) {
    if (widget.isLoading) {
      return const Center(child: CircularProgressIndicator(color: AppColors.cremaAmber));
    }

    final double totalKomisi   = widget.affiliate?.totalKomisi  ?? 0;
    final double saldoTersedia = widget.affiliate?.totalKomisi  ?? 0;
    final double minPayout     = widget.affiliate?.minimumPayout ?? 50000;

    final paidTrx    = widget.transaksiList.where((t) => t.isPaid).toList();
    final pendingKom = widget.transaksiList.where((t) => t.isPending).fold(0.0, (sum, t) => sum + t.komisiAffiliate);

    return RefreshIndicator(
      color: AppColors.cremaAmber,
      backgroundColor: Colors.white,
      onRefresh: widget.onRefresh,
      child: ListView(
        padding: const EdgeInsets.all(20),
        physics: const AlwaysScrollableScrollPhysics(parent: BouncingScrollPhysics()),
        children: [
          const Text(
            'Analisis Komisi Roastery',
            style: TextStyle(fontSize: 20, fontWeight: FontWeight.w800, color: AppColors.textPrimary, letterSpacing: -0.3),
          ),
          const SizedBox(height: 4),
          const Text(
            'Pantau performa pendapatan bagi hasil referral kopi Anda secara berkala',
            style: TextStyle(fontSize: 12, color: AppColors.textSecondary),
          ),
          const SizedBox(height: 20),

          // ── Two Metric Cards ──────────────────────────────────
          Row(
            children: [
              Expanded(
                child: _metricCard(
                  title: 'Total Komisi',
                  amount: CurrencyFormatter.formatRupiah(totalKomisi),
                  icon: Icons.payments_rounded,
                  color: AppColors.espresso,
                  bgColor: AppColors.latteFoam,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: _metricCard(
                  title: 'Menunggu Cair',
                  amount: CurrencyFormatter.formatRupiah(pendingKom),
                  icon: Icons.hourglass_top_rounded,
                  color: AppColors.warning,
                  bgColor: AppColors.warningSurface,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),

          // ── Saldo Siap Ditarik Card ───────────────────────────
          _metricCard(
            title: 'Saldo Komisi Tersedia',
            amount: CurrencyFormatter.formatRupiah(saldoTersedia),
            icon: Icons.account_balance_wallet_rounded,
            color: AppColors.cremaAmber,
            bgColor: AppColors.latteFoam,
            subtitle: saldoTersedia >= minPayout
                ? 'Saldo telah mencukupi batas penarikan minimum'
                : 'Min. penarikan ${CurrencyFormatter.formatRupiah(minPayout)}',
          ),
          const SizedBox(height: 24),

          // ── Performance Chart Container ───────────────────────
          Container(
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(18),
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
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      'Performa 6 Bulan Terakhir',
                      style: TextStyle(fontSize: 15, fontWeight: FontWeight.w800, color: AppColors.textPrimary),
                    ),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                      decoration: BoxDecoration(
                        color: AppColors.latteFoam,
                        borderRadius: BorderRadius.circular(6),
                      ),
                      child: const Text('Komisi (Rp)', style: TextStyle(fontSize: 11, fontWeight: FontWeight.w700, color: AppColors.cremaDark)),
                    ),
                  ],
                ),
                const SizedBox(height: 24),
                SizedBox(
                  height: 190,
                  child: paidTrx.isEmpty
                      ? Center(
                          child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Icon(Icons.bar_chart_rounded, size: 40, color: AppColors.cardBorder),
                              const SizedBox(height: 8),
                              const Text('Belum ada data grafik', style: TextStyle(color: AppColors.textMuted, fontSize: 12)),
                            ],
                          ),
                        )
                      : BarChart(_buildBarChartData(paidTrx)),
                ),
              ],
            ),
          ),
          const SizedBox(height: 20),
        ],
      ),
    );
  }

  Widget _metricCard({
    required String title,
    required String amount,
    required IconData icon,
    required Color color,
    required Color bgColor,
    String? subtitle,
  }) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.cardBorder),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(title, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: AppColors.textSecondary)),
              Container(
                padding: const EdgeInsets.all(7),
                decoration: BoxDecoration(color: bgColor, borderRadius: BorderRadius.circular(8)),
                child: Icon(icon, color: color, size: 16),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Text(amount, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w800, color: AppColors.textPrimary)),
          if (subtitle != null) ...[
            const SizedBox(height: 4),
            Text(subtitle, style: const TextStyle(fontSize: 11, color: AppColors.textMuted)),
          ],
        ],
      ),
    );
  }

  BarChartData _buildBarChartData(List<TransaksiModel> paidTrx) {
    final now = DateTime.now();
    final months = List.generate(6, (i) {
      final d = DateTime(now.year, now.month - 5 + i, 1);
      return {'month': d.month, 'year': d.year, 'label': _monthName(d.month)};
    });

    final monthlyTotals = List<double>.filled(6, 0.0);
    for (final trx in paidTrx) {
      final date = trx.date;
      for (int i = 0; i < 6; i++) {
        if (date.month == months[i]['month'] && date.year == months[i]['year']) {
          monthlyTotals[i] += trx.komisiAffiliate > 0 ? trx.komisiAffiliate : trx.totalHarga * 0.05;
          break;
        }
      }
    }

    double maxY = 100000;
    for (final val in monthlyTotals) {
      if (val > maxY) maxY = val;
    }
    maxY = (maxY * 1.25);

    return BarChartData(
      maxY: maxY,
      barTouchData: BarTouchData(
        touchCallback: (event, response) {
          setState(() {
            if (response?.spot != null) {
              _selectedBarIndex = response!.spot!.touchedBarGroupIndex;
            } else {
              _selectedBarIndex = -1;
            }
          });
        },
        touchTooltipData: BarTouchTooltipData(
          getTooltipItem: (group, groupIndex, rod, rodIndex) {
            return BarTooltipItem(
              '${months[groupIndex]['label']}\n${CurrencyFormatter.formatRupiah(rod.toY)}',
              const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 11),
            );
          },
        ),
      ),
      titlesData: FlTitlesData(
        show: true,
        topTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
        rightTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
        leftTitles: AxisTitles(
          sideTitles: SideTitles(
            showTitles: true,
            reservedSize: 42,
            getTitlesWidget: (val, _) {
              if (val == 0) return const SizedBox.shrink();
              if (val >= 1000000) return Text('${(val / 1000000).toStringAsFixed(1)}jt', style: const TextStyle(fontSize: 10, color: AppColors.textMuted));
              if (val >= 1000) return Text('${(val / 1000).toInt()}k', style: const TextStyle(fontSize: 10, color: AppColors.textMuted));
              return Text('${val.toInt()}', style: const TextStyle(fontSize: 10, color: AppColors.textMuted));
            },
          ),
        ),
        bottomTitles: AxisTitles(
          sideTitles: SideTitles(
            showTitles: true,
            getTitlesWidget: (val, _) {
              final idx = val.toInt();
              if (idx < 0 || idx >= months.length) return const SizedBox.shrink();
              return Padding(
                padding: const EdgeInsets.only(top: 8),
                child: Text(months[idx]['label'] as String, style: const TextStyle(fontSize: 10, fontWeight: FontWeight.w600, color: AppColors.textSecondary)),
              );
            },
          ),
        ),
      ),
      borderData: FlBorderData(show: false),
      gridData: FlGridData(
        show: true,
        drawVerticalLine: false,
        horizontalInterval: maxY / 4,
        getDrawingHorizontalLine: (_) => FlLine(color: AppColors.cardBorder, strokeWidth: 1),
      ),
      barGroups: List.generate(6, (i) {
        final isSelected = _selectedBarIndex == i;
        return BarChartGroupData(
          x: i,
          barRods: [
            BarChartRodData(
              toY: monthlyTotals[i],
              color: isSelected ? AppColors.espresso : AppColors.cremaAmber,
              width: 18,
              borderRadius: const BorderRadius.vertical(top: Radius.circular(6)),
            ),
          ],
        );
      }),
    );
  }

  String _monthName(int m) {
    const names = ['', 'Jan', 'Feb', 'Mar', 'Apr', 'Mei', 'Jun', 'Jul', 'Agu', 'Sep', 'Okt', 'Nov', 'Des'];
    return (m >= 1 && m <= 12) ? names[m] : '';
  }
}