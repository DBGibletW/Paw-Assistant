@Composable
private fun AssistantLogo(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.50f
        val cy = h * 0.50f

        val blue = Color(0xFF4285F4)
        val green = Color(0xFF34A853)
        val yellow = Color(0xFFFBBC04)
        val red = Color(0xFFEA4335)

        // Üst Mavi Geometrik Yaprak
        val topPetal = Path().apply {
            moveTo(cx, h * 0.08f)
            cubicTo(w * 0.65f, h * 0.08f, w * 0.68f, h * 0.24f, w * 0.61f, h * 0.38f)
            lineTo(cx, h * 0.46f)
            lineTo(w * 0.39f, h * 0.38f)
            cubicTo(w * 0.32f, h * 0.24f, w * 0.35f, h * 0.08f, cx, h * 0.08f)
            close()
        }
        drawPath(topPetal, color = blue)

        // Sağ Yeşil Geometrik Yaprak
        val rightPetal = Path().apply {
            moveTo(w * 0.92f, cy)
            cubicTo(w * 0.92f, h * 0.65f, w * 0.76f, h * 0.68f, w * 0.62f, h * 0.61f)
            lineTo(w * 0.54f, cy)
            lineTo(w * 0.62f, h * 0.39f)
            cubicTo(w * 0.76f, h * 0.32f, w * 0.92f, h * 0.35f, w * 0.92f, cy)
            close()
        }
        drawPath(rightPetal, color = green)

        // Alt Sarı Geometrik Yaprak
        val bottomPetal = Path().apply {
            moveTo(cx, h * 0.92f)
            cubicTo(w * 0.35f, h * 0.92f, w * 0.32f, h * 0.76f, w * 0.39f, h * 0.62f)
            lineTo(cx, h * 0.54f)
            lineTo(w * 0.61f, h * 0.62f)
            cubicTo(w * 0.68f, h * 0.76f, w * 0.65f, h * 0.92f, cx, h * 0.92f)
            close()
        }
        drawPath(bottomPetal, color = yellow)

        // Sol Kırmızı Geometrik Yaprak
        val leftPetal = Path().apply {
            moveTo(w * 0.08f, cy)
            cubicTo(w * 0.08f, h * 0.35f, w * 0.24f, h * 0.32f, w * 0.38f, h * 0.39f)
            lineTo(w * 0.46f, cy)
            lineTo(w * 0.38f, h * 0.61f)
            cubicTo(w * 0.24f, h * 0.68f, w * 0.08f, h * 0.65f, w * 0.08f, cy)
            close()
        }
        drawPath(leftPetal, color = red)

        // Merkez Işık Çekirdeği
        drawCircle(
            color = Color.White,
            radius = w * 0.09f,
            center = Offset(cx, cy)
        )
    }
}
