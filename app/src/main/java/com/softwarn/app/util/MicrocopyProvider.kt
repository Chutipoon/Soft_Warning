package com.softwarn.app.util

object MicrocopyProvider {
    private val history = ArrayDeque<Int>(3)

    private val messages = listOf(
        // Humorous
        "Your phone wants a break (but do you?)",
        "TikTok will still be there tomorrow. Pinky promise.",
        "That book you bought is waiting for you.",
        "Your cat misses you (probably).",
        "Your eyes say thank you for the break.",
        "Your body isn't a charging cable.",
        "The world isn't going anywhere. Try stepping back.",
        "This is the most important notification you'll get.",
        "Scroll once more and your thumbs file for retirement.",
        "This app will be here when you wake up.",

        // Thoughtful
        "Take a deep breath. Just one.",
        "Something in the real world is waiting for you.",
        "Time spent is time you can't get back.",
        "Real happiness lives somewhere else.",
        "What matters most in your life?",
        "Try looking out the window for once.",
        "Someone wants to talk to you.",
        "Your body is asking for movement.",
        "Water? Your body is thirsty.",
        "Silence has its own kind of beauty.",

        // Direct
        "Usage limit reached.",
        "Time to take a break.",
        "You set this timer yourself — respect it.",
        "Pause for a moment, then come back.",
        "Session complete. Rest earned.",

        // Reflective
        "Your future self will thank you.",
        "The scroll can wait. You cannot.",
        "One more minute? You've said that 10 times.",
        "Real life called. It misses you.",
        "Break time. Non-negotiable.",
        "Is this how you want to spend your time?",
        "Boredom is just creativity waiting to happen.",
        "Your phone knows when you've had enough.",
        "Less screen. More life.",
        "You deserve a break."
    )

    fun random(): String {
        val available = messages.indices.filter { it !in history }
        val idx = available.random()
        if (history.size >= 3) history.removeFirst()
        history.addLast(idx)
        return messages[idx]
    }
}
