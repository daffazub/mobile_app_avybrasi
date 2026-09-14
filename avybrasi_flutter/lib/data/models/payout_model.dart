class PayoutModel {
  final String? idRequest;
  final String idAffiliate;
  final double jumlah;
  final String status;
  final String? keteranganAdmin;
  final String? approvedBy;
  final String? buktiTransferUrl;
  final String? createdAt;
  final String? updatedAt;

  PayoutModel({
    this.idRequest,
    required this.idAffiliate,
    required this.jumlah,
    this.status = 'pending',
    this.keteranganAdmin,
    this.approvedBy,
    this.buktiTransferUrl,
    this.createdAt,
    this.updatedAt,
  });

  factory PayoutModel.fromJson(Map<String, dynamic> json) {
    return PayoutModel(
      idRequest: json['id_request'] as String?,
      idAffiliate: json['id_affiliate'] as String,
      jumlah: (json['jumlah'] as num?)?.toDouble() ?? 0.0,
      status: (json['status'] as String?) ?? 'pending',
      keteranganAdmin: json['keterangan_admin'] as String?,
      approvedBy: json['approved_by'] as String?,
      buktiTransferUrl: json['bukti_transfer_url'] as String?,
      createdAt: json['created_at'] as String?,
      updatedAt: json['updated_at'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (idRequest != null) 'id_request': idRequest,
      'id_affiliate': idAffiliate,
      'jumlah': jumlah,
      'status': status,
      if (keteranganAdmin != null) 'keterangan_admin': keteranganAdmin,
      if (approvedBy != null) 'approved_by': approvedBy,
      if (buktiTransferUrl != null) 'bukti_transfer_url': buktiTransferUrl,
    };
  }
}
