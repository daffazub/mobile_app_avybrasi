import 'package:flutter/foundation.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import '../../data/models/profile_model.dart';
import '../../data/models/affiliate_model.dart';
import '../../data/models/payout_model.dart';
import '../../data/models/transaksi_model.dart';

class SupabaseService {
  SupabaseService._();

  static const String _supabaseUrl    = 'https://dtjvahuoxwtagdwziibh.supabase.co';
  static const String _publishableKey =
      'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImR0anZhaHVveHd0YWdkd3ppaWJoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzY3NDE1ODcsImV4cCI6MjA5MjMxNzU4N30.a9g_HuWbydLp-N9OAK5bYhPrTXOsxEPDtvEuEfDnJCU';

  static SupabaseClient get client => Supabase.instance.client;

  static Future<void> initialize() async {
    // ignore: deprecated_member_use
    await Supabase.initialize(url: _supabaseUrl, anonKey: _publishableKey);
  }

  static User?   get currentUser   => client.auth.currentUser;
  static String? get currentUserId => client.auth.currentUser?.id;
  static bool    get isLoggedIn    => client.auth.currentSession != null;
  static Stream<AuthState> get authStateChanges => client.auth.onAuthStateChange;

  static Future<AuthResponse> signIn({required String email, required String password}) =>
      client.auth.signInWithPassword(email: email, password: password);

  static Future<void> signOut() => client.auth.signOut();

  static Future<void> resetPassword({required String email}) =>
      client.auth.resetPasswordForEmail(email);

  static Future<String> resolveUserRole() async {
    final uid = currentUserId;
    if (uid == null) return 'user';
    try {
      final adminRow = await client
          .from('admin_profiles')
          .select('level_admin')
          .eq('user_id', uid)
          .maybeSingle();
      if (adminRow != null) {
        // Kembalikan level_admin langsung sesuai nilai di DB
        return (adminRow['level_admin'] as String?) ?? 'admin';
      }
      final affRow = await client
          .from('affiliate_profiles')
          .select('id_affiliate')
          .eq('user_id', uid)
          .maybeSingle();
      if (affRow != null) return 'affiliate';
      return 'user';
    } catch (e) {
      debugPrint('[SupabaseService] resolveUserRole error: $e');
      return 'user';
    }
  }

  static Future<ProfileModel?> getUserProfile() async {
    final uid = currentUserId;
    if (uid == null) return null;
    try {
      final res = await client
          .from('profiles')
          .select()
          .eq('user_id', uid)
          .maybeSingle();
      return res != null ? ProfileModel.fromJson(res) : null;
    } catch (e) {
      debugPrint('[SupabaseService] getUserProfile error: $e');
      return null;
    }
  }

  static Future<AffiliateModel?> getAffiliateProfile() async {
    final uid = currentUserId;
    if (uid == null) return null;
    try {
      final res = await client
          .from('affiliate_profiles')
          .select()
          .eq('user_id', uid)
          .maybeSingle();
      return res != null ? AffiliateModel.fromJson(res) : null;
    } catch (e) {
      debugPrint('[SupabaseService] getAffiliateProfile error: $e');
      return null;
    }
  }

  static Future<bool> updatePaymentMethod({
    required String affiliateId,
    required String bank,
    required String nomorRekening,
    required String atasNama,
  }) async {
    try {
      await client.from('affiliate_profiles').update({
        'payment_method': {
          'bank': bank,
          'nomor_rekening': nomorRekening,
          'atas_nama': atasNama,
        },
        'updated_at': DateTime.now().toIso8601String(),
      }).eq('id_affiliate', affiliateId);
      return true;
    } catch (e) {
      debugPrint('[SupabaseService] updatePaymentMethod error: $e');
      return false;
    }
  }

  static Future<List<PayoutModel>> getPayoutRequests(String idAffiliate) async {
    if (idAffiliate.isEmpty) return [];
    try {
      final res = await client
          .from('payout_requests')
          .select()
          .eq('id_affiliate', idAffiliate)
          .order('created_at', ascending: false);
      return (res as List)
          .map((e) => PayoutModel.fromJson(e as Map<String, dynamic>))
          .toList();
    } catch (e) {
      debugPrint('[SupabaseService] getPayoutRequests error: $e');
      return [];
    }
  }

  static Future<bool> hasPendingPayout(String idAffiliate) async {
    if (idAffiliate.isEmpty) return false;
    try {
      final res = await client
          .from('payout_requests')
          .select('id_request')
          .eq('id_affiliate', idAffiliate)
          .eq('status', 'pending')
          .limit(1);
      return (res as List).isNotEmpty;
    } catch (e) {
      debugPrint('[SupabaseService] hasPendingPayout error: $e');
      return false;
    }
  }

  static Future<bool> insertPayoutRequest({
    required String idAffiliate,
    required double jumlah,
  }) async {
    try {
      await client.from('payout_requests').insert({
        'id_affiliate': idAffiliate,
        'jumlah': jumlah,
        'status': 'pending',
      });
      return true;
    } catch (e) {
      debugPrint('[SupabaseService] insertPayoutRequest error: $e');
      return false;
    }
  }

  /// Ambil transaksi berdasarkan [idAffiliate].
  /// Parameter [profileId] diabaikan karena kolom FK di tabel transaksi
  /// adalah id_affiliate, bukan profile id.
  static Future<List<TransaksiModel>> getTransaksiAffiliate({
    String? profileId,
    String? idAffiliate,
  }) async {
    final id = idAffiliate ?? profileId;
    if (id == null || id.isEmpty) return [];
    try {
      final res = await client
          .from('transaksi')
          .select()
          .eq('id_affiliate', id)
          .order('created_at', ascending: false);
      return (res as List)
          .map((e) => TransaksiModel.fromJson(e as Map<String, dynamic>))
          .toList();
    } catch (e) {
      debugPrint('[SupabaseService] getTransaksiAffiliate error: $e');
      return [];
    }
  }
}