package app.auf

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform