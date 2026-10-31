# Kandado (Android)

Nila-lock ang **totoong apps** sa phone mo gamit ang iniikot na dial.

- Unang bukas: gumawa ng 4-numerong code (isang beses lang). Mapapalitan ito sa loob ng Vault.
- Sa Vault, i-on ang switch ng app na gusto mong i-lock.
- Kapag binuksan ang naka-lock na app, lalabas ang dial. Tama ang code = bukas, at mananatiling bukas hanggang lumipat ka sa ibang app.
- Hindi maalis sa home screen ang icon ng app. Ang lock lang ang idinadagdag.

## Kumuha ng APK
1. I-push ang buong folder na ito sa GitHub repo (kasama ang `.github`).
2. Tab na **Actions** → **Build APK** → hintayin matapos.
3. I-download ang **Kandado-apk** sa Artifacts, i-extract, at i-install ang `app-debug.apk` sa phone.

## Pagkatapos i-install
1. Buksan ang Kandado at gumawa ng code.
2. Pindutin ang **I-on sa Accessibility** → piliin ang **Kandado** → i-on.
   - Kung naka-gray (Android 13+): **Settings → Apps → Kandado → ⋮ (itaas na kanan) → Allow restricted settings**, tapos ulitin.
3. I-on ang switch ng mga app na gusto mong i-lock at subukang buksan ang isa.

Kung hindi lumalabas ang dial sa ilang brand (Xiaomi, Oppo, Vivo, Realme), payagan ang Kandado sa **Autostart** at **Display pop-up windows while running in background**, at tanggalin ito sa battery optimization.
