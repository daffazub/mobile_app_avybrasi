import 'package:intl/intl.dart';

class CurrencyFormatter {
  static final NumberFormat _rupiahFormat = NumberFormat.currency(
    locale: 'id_ID',
    symbol: 'Rp ',
    decimalDigits: 0,
  );

  static String formatRupiah(num? amount) {
    if (amount == null) return 'Rp 0';
    return _rupiahFormat.format(amount);
  }

  static String formatTanggal(String? dateStr) {
    if (dateStr == null || dateStr.isEmpty) return '-';
    try {
      DateTime dt;
      if (dateStr.contains('T')) {
        dt = DateTime.parse(dateStr).toLocal();
      } else {
        dt = DateFormat('yyyy-MM-dd HH:mm:ss').parse(dateStr).toLocal();
      }
      return DateFormat('dd MMM yyyy, HH:mm', 'id_ID').format(dt);
    } catch (_) {
      try {
        final dt = DateTime.parse(dateStr);
        return DateFormat('dd MMM yyyy', 'id_ID').format(dt);
      } catch (_) {
        return dateStr.length > 10 ? dateStr.substring(0, 10) : dateStr;
      }
    }
  }

  static String formatDateTime(dynamic date) {
    if (date == null) return '-';
    if (date is DateTime) {
      return DateFormat('dd MMM yyyy, HH:mm', 'id_ID').format(date);
    }
    return formatTanggal(date.toString());
  }
}
