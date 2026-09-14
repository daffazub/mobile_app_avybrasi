class AffiliateModel {
  final String idAffiliate;
  final String userId;
  final String? namaLengkap;
  final String kodeReferal;
  final double komisiPersen;
  final double totalKomisi;
  final double minimumPayout;
  final String statusAffiliate;
  final Map<String, dynamic>? paymentMethod;
  final String? createdAt;
  final String? updatedAt;

  AffiliateModel({
    required this.idAffiliate,
    required this.userId,
    this.namaLengkap,
    required this.kodeReferal,
    this.komisiPersen = 5.0,
    this.totalKomisi = 0.0,
    this.minimumPayout = 100000.0,
    this.statusAffiliate = 'active',
    this.paymentMethod,
    this.createdAt,
    this.updatedAt,
  });

  // Getters for convenience
  bool get isActive => statusAffiliate == 'active';

  String get bank => paymentMethod?['bank']?.toString() ?? '';
  String get nomorRekening => paymentMethod?['nomor_rekening']?.toString() ?? paymentMethod?['no_rekening']?.toString() ?? '';
  String get atasNama => paymentMethod?['atas_nama']?.toString() ?? '';

  bool get isPaymentMethodComplete =>
      bank.isNotEmpty && nomorRekening.isNotEmpty && atasNama.isNotEmpty;

  factory AffiliateModel.fromJson(Map<String, dynamic> json) {
    return AffiliateModel(
      idAffiliate: json['id_affiliate'] as String,
      userId: json['user_id'] as String,
      namaLengkap: json['nama_lengkap'] as String?,
      kodeReferal: (json['kode_referal'] as String?) ?? '-',
      komisiPersen: (json['komisi_persen'] as num?)?.toDouble() ?? 5.0,
      totalKomisi: (json['total_komisi'] as num?)?.toDouble() ?? 0.0,
      minimumPayout: (json['minimum_payout'] as num?)?.toDouble() ?? 100000.0,
      statusAffiliate: (json['status_affiliate'] as String?) ?? 'active',
      paymentMethod: json['payment_method'] is Map<String, dynamic>
          ? json['payment_method'] as Map<String, dynamic>
          : null,
      createdAt: json['created_at'] as String?,
      updatedAt: json['updated_at'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id_affiliate': idAffiliate,
      'user_id': userId,
      if (namaLengkap != null) 'nama_lengkap': namaLengkap,
      'kode_referal': kodeReferal,
      'komisi_persen': komisiPersen,
      'total_komisi': totalKomisi,
      'minimum_payout': minimumPayout,
      'status_affiliate': statusAffiliate,
      if (paymentMethod != null) 'payment_method': paymentMethod,
    };
  }
}
