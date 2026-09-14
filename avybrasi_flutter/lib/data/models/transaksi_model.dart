class TransaksiModel {
  final String? idTransaksi;
  final String? noInvoice;
  final double totalHarga;
  final double komisiAffiliate;
  final String status;
  final String? createdAt;

  TransaksiModel({
    this.idTransaksi,
    this.noInvoice,
    this.totalHarga = 0.0,
    this.komisiAffiliate = 0.0,
    this.status = 'pending',
    this.createdAt,
  });

  bool get isPaid => status == 'paid' || status == 'delivered';
  bool get isPending => status == 'pending' || status == 'processed' || status == 'shipped';

  DateTime get date {
    if (createdAt == null) return DateTime.now();
    try {
      return DateTime.parse(createdAt!);
    } catch (_) {
      return DateTime.now();
    }
  }

  factory TransaksiModel.fromJson(Map<String, dynamic> json) {
    return TransaksiModel(
      idTransaksi: json['id_transaksi'] as String?,
      noInvoice: json['no_invoice'] as String?,
      totalHarga: (json['total_harga'] as num?)?.toDouble() ?? 0.0,
      komisiAffiliate: (json['komisi_affiliate'] as num?)?.toDouble() ?? 0.0,
      status: (json['status'] as String?) ?? 'pending',
      createdAt: json['created_at'] as String?,
    );
  }
}
