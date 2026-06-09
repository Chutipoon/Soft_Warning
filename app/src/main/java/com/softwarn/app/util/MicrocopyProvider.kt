package com.softwarn.app.util

object MicrocopyProvider {
    private val history = ArrayDeque<Int>(3)

    private val messages = listOf(
        // Humorous (Thai)
        "โทรศัพท์คุณอยากพักแล้ว (แต่คุณล่ะ?)",
        "TikTok ยังอยู่พรุ่งนี้นะ สัญญา",
        "หนังสือที่ซื้อไว้รอคุณอยู่นะ",
        "แมวคุณคิดถึง (ถ้ามี)",
        "สายตาขอบคุณถ้าคุณพัก",
        "ร่างกายคุณไม่ใช่ชาร์จเจอร์นะ",
        "โลกยังไม่หายไปไหน ลองพักดู",
        "notification ที่สำคัญที่สุดคืออันนี้",
        "ถ้าเลื่อนหน้าจออีก นิ้วจะขอลาออก",
        "แอปนี้จะยังอยู่ตอนตื่นเช้า",
        // Thoughtful (Thai)
        "ลองหายใจลึกๆ สักครั้งไหม",
        "มีอะไรในโลกจริงรอคุณอยู่",
        "เวลาที่ใช้ไปแล้วเรียกคืนไม่ได้",
        "ความสุขที่ยั่งยืนอยู่ที่อื่น",
        "สิ่งที่สำคัญในชีวิตคุณคืออะไร?",
        "ลองมองออกนอกหน้าต่างสักครั้ง",
        "ใครคนหนึ่งอยากคุยกับคุณ",
        "ร่างกายต้องการการเคลื่อนไหว",
        "ดื่มน้ำสักแก้วก่อนไหม",
        "ความเงียบสงบก็มีคุณค่า",
        // Direct (Thai)
        "ใช้งานครบเวลาที่ตั้งไว้แล้ว",
        "เวลาหยุดพักถึงแล้ว",
        "คุณตั้งเวลาไว้เองนะ — เคารพตัวเอง",
        "หยุดพักสักครู่แล้วค่อยกลับมา",
        "ครบ session แล้ว พักได้",
        // English bonus
        "Your future self will thank you",
        "The scroll can wait. You cannot.",
        "One more minute? You've said that 10 times.",
        "Real life called. It misses you.",
        "Break time. Non-negotiable."
    )

    fun random(): String {
        val available = messages.indices.filter { it !in history }
        val idx = available.random()
        if (history.size >= 3) history.removeFirst()
        history.addLast(idx)
        return messages[idx]
    }
}
