class ProfileModel {
  final String? id;
  final String userId;
  final String? username;
  final String? fullName;
  final String? avatarUrl;
  final String? phone;
  final String? createdAt;
  final String? updatedAt;

  ProfileModel({
    this.id,
    required this.userId,
    this.username,
    this.fullName,
    this.avatarUrl,
    this.phone,
    this.createdAt,
    this.updatedAt,
  });

  factory ProfileModel.fromJson(Map<String, dynamic> json) {
    return ProfileModel(
      id: json['id'] as String?,
      userId: json['user_id'] as String,
      username: json['username'] as String?,
      fullName: json['full_name'] as String?,
      avatarUrl: json['avatar_url'] as String?,
      phone: json['phone'] as String?,
      createdAt: json['created_at'] as String?,
      updatedAt: json['updated_at'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (id != null) 'id': id,
      'user_id': userId,
      if (username != null) 'username': username,
      if (fullName != null) 'full_name': fullName,
      if (avatarUrl != null) 'avatar_url': avatarUrl,
      if (phone != null) 'phone': phone,
    };
  }
}
