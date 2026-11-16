package com.cybersentinel.app.data.model

/**
 * Three core defense vectors of Cyber Sentinel.
 */
enum class ProtectionLayer(
    val title: String,
    val subtitle: String,
    val badge: String
) {
    LAYER_1(
        title = "Layer 1: Voice & Call",
        subtitle = "Live Speech & Digital Arrest Shield",
        badge = "Voice AI"
    ),
    LAYER_2(
        title = "Layer 2: Message & Links",
        subtitle = "WhatsApp & SMS Phishing Scanner",
        badge = "URL Heuristics"
    ),
    LAYER_3(
        title = "Layer 3: Guardian SOS",
        subtitle = "Multi-Guardian Emergency Network",
        badge = "Room Sync"
    )
}

