import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'core/constants/app_theme.dart';
import 'core/services/supabase_service.dart';
import 'presentation/auth/role_gatekeeper.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Set preferred orientation & status bar styling
  await SystemChrome.setPreferredOrientations([
    DeviceOrientation.portraitUp,
    DeviceOrientation.portraitDown,
  ]);
  SystemChrome.setSystemUIOverlayStyle(
    const SystemUiOverlayStyle(
      statusBarColor: Colors.transparent,
      statusBarIconBrightness: Brightness.dark,
    ),
  );

  // Initialize Supabase Client
  await SupabaseService.initialize();

  runApp(const AvybrasiApp());
}

class AvybrasiApp extends StatelessWidget {
  const AvybrasiApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Avybrasi Affiliate',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      home: const RoleGatekeeper(),
    );
  }
}
