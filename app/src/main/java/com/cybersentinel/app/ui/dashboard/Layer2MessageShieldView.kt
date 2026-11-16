package com.cybersentinel.app.ui.dashboard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybersentinel.app.data.model.ThreatLevel
import com.cybersentinel.app.data.model.UrlScanAnalysis
import com.cybersentinel.app.ui.components.ScanningLineOverlay
import com.cybersentinel.app.ui.components.ThreatRiskGauge
import com.cybersentinel.app.ui.components.ThreatStatusBadge
import com.cybersentinel.app.ui.theme.CriticalCrimsonAlert
import com.cybersentinel.app.ui.theme.CyanGlow
import com.cybersentinel.app.ui.theme.ElectricCyan
import com.cybersentinel.app.ui.theme.LuminousPurple
import com.cybersentinel.app.ui.theme.MidnightIndigo
import com.cybersentinel.app.ui.theme.TextMuted
import com.cybersentinel.app.ui.theme.TextPrimary
import com.cybersentinel.app.ui.theme.TextSecondary
import com.cybersentinel.app.ui.theme.TwilightSurfaceVariant
import com.cybersentinel.app.ui.theme.TwilightVioletCard
import com.cybersentinel.app.ui.theme.WarmAmberWarning
import com.cybersentinel.app.util.UrlThreatEngine
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * LAYER 2: Real Screenshot OCR & QR Code Scanner (Google ML Kit)
 * - Native Image Picker via ActivityResultContracts.GetContent("image/all")
 * - On-Device ML Kit Text Recognition & Barcode Scanning (zero cloud reliance)
 * - Animated scanning line overlay while processing bitmap
 * - Direct extraction of QR payloads and OCR text passed to Threat Engine
 */
@Composable
fun Layer2MessageShieldView(
    onAnalyzeInLayer3: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    var ocrResultText by remember { mutableStateOf<String?>(null) }
    var decodedQrPayload by remember { mutableStateOf<String?>(null) }
    var threatAnalysis by remember { mutableStateOf<UrlScanAnalysis?>(null) }

    // Native ActivityResult Launcher for picking gallery screenshots or camera images
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            isProcessing = true
            ocrResultText = null
            decodedQrPayload = null
            threatAnalysis = null

            coroutineScope.launch {
                val bitmap = loadBitmapFromUri(context, uri)
                selectedBitmap = bitmap

                if (bitmap != null) {
                    processImageWithMLKit(
                        bitmap = bitmap,
                        onSuccess = { extractedText, qrCode ->
                            isProcessing = false
                            ocrResultText = extractedText
                            decodedQrPayload = qrCode

                            // Automatically pass extracted URL or text to the Threat Engine
                            val targetForAnalysis = qrCode ?: extractedText ?: ""
                            if (targetForAnalysis.isNotBlank()) {
                                threatAnalysis = UrlThreatEngine.analyze(targetForAnalysis)
                            }
                        },
                        onError = { errorMsg ->
                            isProcessing = false
                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    )
                } else {
                    isProcessing = false
                    Toast.makeText(context, "Failed to load selected image bitmap", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Layer 2 Header Overview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = TwilightVioletCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(ElectricCyan.copy(alpha = 0.35f))
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(LuminousPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "ML Kit Scanner",
                        tint = ElectricCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Real Screenshot OCR & QR Decoder",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Google ML Kit extracts on-device text from WhatsApp screenshots, suspicious SMS receipts, and decodes QR codes before you tap.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Image Picker Trigger Button
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricCyan,
                contentColor = MidnightIndigo
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = "Pick Image",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Pick Screenshot or QR from Gallery",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Image Preview & Scanning Line Animation Box
        if (selectedBitmap != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TwilightVioletCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isProcessing) ElectricCyan else Color(0xFF334155)
                    )
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isProcessing) "ANALYZING BITMAP VIA ML KIT..." else "IMAGE INSPECTION COMPLETE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isProcessing) ElectricCyan else CyanGlow,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = ElectricCyan,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MidnightIndigo),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "Selected Screenshot",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Animated Scanning Line Overlay
                        ScanningLineOverlay(
                            isScanning = isProcessing,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Decoded QR Code Payload Section
        if (decodedQrPayload != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(ElectricCyan)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DECODED QR CODE PAYLOAD",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(decodedQrPayload!!))
                                Toast.makeText(context, "QR Link copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = decodedQrPayload!!,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Extracted OCR Text Section
        if (ocrResultText != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TwilightVioletCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ImageSearch,
                                contentDescription = null,
                                tint = LuminousPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EXTRACTED ON-DEVICE OCR TEXT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = LuminousPurple
                                )
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(ocrResultText!!))
                                Toast.makeText(context, "OCR text copied", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MidnightIndigo)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = if (ocrResultText!!.isBlank()) "No alphanumeric text found in image." else ocrResultText!!,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Layer 3 Threat Assessment of Extracted Data
        if (threatAnalysis != null) {
            Text(
                text = "AUTOMATIC LAYER 3 THREAT VERDICT",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = CyanGlow,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            ThreatRiskGauge(
                score = threatAnalysis!!.riskScore,
                title = "ML KIT PAYLOAD RISK GAUGE"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (threatAnalysis!!.riskCategory) {
                        ThreatLevel.CRITICAL_ALERT -> Color(0xFF450A0A)
                        ThreatLevel.SUSPICIOUS -> Color(0xFF451A03)
                        ThreatLevel.SAFE -> TwilightVioletCard
                    }
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SECURITY AUDIT BREAKDOWN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        ThreatStatusBadge(threatLevel = threatAnalysis!!.riskCategory)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    threatAnalysis!!.ruleExplanations.forEach { rule ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("•", color = ElectricCyan, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = rule,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Quick Demonstration Samples
        Text(
            text = "DEMONSTRATION OCR & QR USE CASES",
            style = MaterialTheme.typography.labelSmall.copy(
                color = LuminousPurple,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val fakeText = "Urgent: Your SBI YONO account is locked. Update PAN immediately at https://sbi-card-kyc.xyz or power will be cut."
                    ocrResultText = fakeText
                    threatAnalysis = UrlThreatEngine.analyze("https://sbi-card-kyc.xyz")
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Simulate OCR", fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = {
                    val fakeQr = "http://192.168.1.55/update-ebill.apk"
                    decodedQrPayload = fakeQr
                    threatAnalysis = UrlThreatEngine.analyze(fakeQr)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Simulate QR", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

private suspend fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (_: Exception) {
        null
    }
}

private fun processImageWithMLKit(
    bitmap: Bitmap,
    onSuccess: (extractedText: String?, qrPayload: String?) -> Unit,
    onError: (String) -> Unit
) {
    val image = InputImage.fromBitmap(bitmap, 0)

    val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val barcodeScanner = BarcodeScanning.getClient()

    barcodeScanner.process(image)
        .addOnSuccessListener { barcodes ->
            var qrCodeUrl: String? = null
            for (barcode in barcodes) {
                if (barcode.valueType == Barcode.TYPE_URL || barcode.format == Barcode.FORMAT_QR_CODE) {
                    qrCodeUrl = barcode.url?.url ?: barcode.rawValue
                    if (qrCodeUrl != null) break
                }
            }

            // Next run OCR text recognition
            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    onSuccess(visionText.text, qrCodeUrl)
                }
                .addOnFailureListener {
                    // Even if OCR fails, return QR if decoded
                    onSuccess(null, qrCodeUrl)
                }
        }
        .addOnFailureListener {
            // If barcode scan fails, try OCR
            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    onSuccess(visionText.text, null)
                }
                .addOnFailureListener { e ->
                    onError("ML Kit processing error: ${e.message}")
                }
        }
}
