import 'package:flutter/material.dart';
import '../../core/constants/app_colors.dart';
import '../../core/services/supabase_service.dart';
import '../../data/models/affiliate_model.dart';
import '../../data/models/payout_model.dart';
import '../../data/models/profile_model.dart';
import '../../data/models/transaksi_model.dart';
import '../common/responsive_container.dart';
import '../common/vybrasi_logo.dart';
import 'tabs/aff_home_tab.dart';
import 'tabs/aff_transaksi_tab.dart';
import 'tabs/aff_komisi_tab.dart';
import 'tabs/aff_profil_tab.dart';

class AffiliateDashboardScreen extends StatefulWidget {
  const AffiliateDashboardScreen({super.key});
  @override
  State<AffiliateDashboardScreen> createState() => _AffiliateDashboardScreenState();
}

class _AffiliateDashboardScreenState extends State<AffiliateDashboardScreen> {
  int  _currentIndex  = 0;
  bool _isLoading     = true;

  ProfileModel?        _profile;
  AffiliateModel?      _affiliate;
  List<TransaksiModel> _transaksiList    = [];
  List<PayoutModel>    _payoutList       = [];
  bool                 _hasPendingPayout = false;

  @override
  void initState() {
    super.initState();
    _loadAll();
  }

  Future<void> _loadAll() async {
    setState(() => _isLoading = true);
    try {
      final results = await Future.wait([
        SupabaseService.getUserProfile(),
        SupabaseService.getAffiliateProfile(),
      ]);

      final profile   = results[0] as ProfileModel?;
      final affiliate = results[1] as AffiliateModel?;

      List<TransaksiModel> transaksiList    = [];
      List<PayoutModel>    payoutList       = [];
      bool                 hasPendingPayout = false;

      if (affiliate != null) {
        final secondary = await Future.wait([
          SupabaseService.getTransaksiAffiliate(profileId: profile?.id, idAffiliate: affiliate.idAffiliate),
          SupabaseService.getPayoutRequests(affiliate.idAffiliate),
          SupabaseService.hasPendingPayout(affiliate.idAffiliate),
        ]);
        transaksiList    = secondary[0] as List<TransaksiModel>;
        payoutList       = secondary[1] as List<PayoutModel>;
        hasPendingPayout = secondary[2] as bool;
      }

      if (mounted) {
        setState(() {
          _profile          = profile;
          _affiliate        = affiliate;
          _transaksiList    = transaksiList;
          _payoutList       = payoutList;
          _hasPendingPayout = hasPendingPayout;
          _isLoading        = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() => _isLoading = false);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Gagal memuat data. Silakan periksa koneksi internet Anda.'),
            backgroundColor: AppColors.error,
          ),
        );
      }
    }
  }

  Future<void> _handleLogout() => SupabaseService.signOut();
  void _goToTab(int index) => setState(() => _currentIndex = index);

  @override
  Widget build(BuildContext context) {
    final tabs = <Widget>[
      AffHomeTab(
        profile: _profile,
        affiliate: _affiliate,
        transaksiList: _transaksiList,
        isLoading: _isLoading,
        onRefresh: _loadAll,
        onWithdrawTap: () => _goToTab(1),
      ),
      AffTransaksiTab(
        transaksiList: _transaksiList,
        affiliate: _affiliate,
        isLoading: _isLoading,
        hasPendingPayout: _hasPendingPayout,
        onRefresh: _loadAll,
        onWithdrawSuccess: _loadAll,
      ),
      AffKomisiTab(
        affiliate: _affiliate,
        transaksiList: _transaksiList,
        isLoading: _isLoading,
        onRefresh: _loadAll,
      ),
      AffProfilTab(
        profile: _profile,
        affiliate: _affiliate,
        payoutList: _payoutList,
        isLoading: _isLoading,
        onRefresh: _loadAll,
        onLogout: _handleLogout,
      ),
    ];

    return Scaffold(
      backgroundColor: AppColors.scaffoldBg,
      body: ResponsiveContainer(
        child: SafeArea(
          child: Column(
            children: [
              // Top Roastery App Brand Bar
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
                decoration: BoxDecoration(
                  color: AppColors.espresso,
                  boxShadow: [
                    BoxShadow(
                      color: AppColors.espresso.withValues(alpha: 0.12),
                      blurRadius: 10,
                      offset: const Offset(0, 3),
                    ),
                  ],
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const VybrasiLogo(
                      size: 28,
                      variant: VybrasiLogoVariant.horizontal,
                      dark: true,
                    ),
                    IconButton(
                      icon: const Icon(Icons.refresh_rounded, color: AppColors.cremaAmber, size: 22),
                      tooltip: 'Muat Ulang Data',
                      onPressed: _isLoading ? null : _loadAll,
                    ),
                  ],
                ),
              ),

              // Active Tab Content
              Expanded(
                child: IndexedStack(
                  index: _currentIndex,
                  children: tabs,
                ),
              ),

              // Bottom Roastery Navigation Bar
              Container(
                decoration: BoxDecoration(
                  color: Colors.white,
                  border: const Border(top: BorderSide(color: AppColors.cardBorder, width: 1)),
                  boxShadow: [
                    BoxShadow(
                      color: AppColors.espresso.withValues(alpha: 0.04),
                      blurRadius: 12,
                      offset: const Offset(0, -3),
                    ),
                  ],
                ),
                child: NavigationBar(
                  backgroundColor: Colors.white,
                  selectedIndex: _currentIndex,
                  onDestinationSelected: _goToTab,
                  height: 66,
                  destinations: const [
                    NavigationDestination(
                      icon: Icon(Icons.storefront_outlined),
                      selectedIcon: Icon(Icons.storefront_rounded),
                      label: 'Beranda',
                    ),
                    NavigationDestination(
                      icon: Icon(Icons.receipt_long_outlined),
                      selectedIcon: Icon(Icons.receipt_long_rounded),
                      label: 'Pesanan',
                    ),
                    NavigationDestination(
                      icon: Icon(Icons.analytics_outlined),
                      selectedIcon: Icon(Icons.analytics_rounded),
                      label: 'Komisi',
                    ),
                    NavigationDestination(
                      icon: Icon(Icons.person_outline_rounded),
                      selectedIcon: Icon(Icons.person_rounded),
                      label: 'Akun',
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}