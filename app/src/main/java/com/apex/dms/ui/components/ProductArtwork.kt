package com.apex.dms.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.apex.dms.data.Product
import com.apex.dms.ui.theme.ShoppeBlue
import com.apex.dms.ui.theme.ShoppeBlueSoft
import com.apex.dms.ui.theme.ShoppeLilac
import com.apex.dms.ui.theme.ShoppeMint
import com.apex.dms.ui.theme.ShoppePeach
import com.apex.dms.ui.theme.ShoppeSky

@Composable
fun ProductArtwork(product: Product, modifier: Modifier = Modifier) {
    val background = when (product.categoryId) {
        "cat-bearings" -> ShoppeBlueSoft
        "cat-vbelts", "cat-timing" -> ShoppeLilac
        "cat-fasteners" -> ShoppeSky
        "cat-contactors", "cat-relays" -> ShoppeMint
        "cat-consumables" -> ShoppePeach
        else -> Color(0xFFF1F4F9)
    }

    Box(
        modifier = modifier.background(background, RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (product.imageUrl.orEmpty().isNotBlank()) {
            SubcomposeAsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier.fillMaxSize(),
            ) {
                when (painter.state) {
                    is coil.compose.AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                    else -> ProductFallback(product)
                }
            }
        } else {
            ProductFallback(product)
        }
    }
}

@Composable
private fun ProductFallback(product: Product) {
    Canvas(Modifier.fillMaxSize()) {
        val ink = ShoppeBlue.copy(alpha = 0.82f)
        val w = size.width
        val h = size.height
        when (product.categoryId) {
            "cat-bearings" -> {
                drawCircle(ink, radius = w * 0.24f, center = Offset(w * .5f, h * .5f), style = Stroke(width = w * .055f))
                drawCircle(ink.copy(alpha = .45f), radius = w * 0.09f, center = Offset(w * .5f, h * .5f), style = Stroke(width = w * .035f))
            }
            "cat-vbelts", "cat-timing" -> {
                val path = Path().apply {
                    moveTo(w * .28f, h * .28f)
                    cubicTo(w * .12f, h * .5f, w * .27f, h * .75f, w * .48f, h * .73f)
                    cubicTo(w * .74f, h * .70f, w * .84f, h * .42f, w * .68f, h * .28f)
                    cubicTo(w * .58f, h * .18f, w * .39f, h * .17f, w * .28f, h * .28f)
                }
                drawPath(path, ink, style = Stroke(width = w * .055f, cap = StrokeCap.Round))
            }
            "cat-fasteners" -> {
                drawRoundRect(ink, topLeft = Offset(w*.45f, h*.20f), size = Size(w*.12f, h*.50f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w*.03f), style = Stroke(width = w*.04f))
                drawCircle(ink, radius = w*.14f, center = Offset(w*.51f,h*.24f), style = Stroke(width = w*.04f))
                repeat(4) { i -> drawLine(ink.copy(alpha=.55f), Offset(w*.42f,h*(.42f+i*.07f)), Offset(w*.60f,h*(.42f+i*.07f)), strokeWidth = w*.025f) }
            }
            "cat-contactors", "cat-relays" -> {
                drawRoundRect(ink, topLeft = Offset(w*.27f,h*.22f), size = Size(w*.46f,h*.56f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w*.05f), style = Stroke(width=w*.035f))
                repeat(3) { i ->
                    drawCircle(ink.copy(alpha=.55f), radius=w*.035f, center=Offset(w*(.38f+i*.12f),h*.36f))
                    drawLine(ink.copy(alpha=.55f),Offset(w*(.38f+i*.12f),h*.48f),Offset(w*(.38f+i*.12f),h*.64f),strokeWidth=w*.025f,cap=StrokeCap.Round)
                }
            }
            "cat-consumables" -> {
                drawCircle(ink, radius=w*.25f, center=Offset(w*.5f,h*.5f), style=Stroke(width=w*.045f))
                drawCircle(ink.copy(alpha=.5f), radius=w*.06f, center=Offset(w*.5f,h*.5f), style=Stroke(width=w*.025f))
                drawLine(ink.copy(alpha=.5f),Offset(w*.33f,h*.33f),Offset(w*.67f,h*.67f),strokeWidth=w*.025f)
                drawLine(ink.copy(alpha=.5f),Offset(w*.67f,h*.33f),Offset(w*.33f,h*.67f),strokeWidth=w*.025f)
            }
            else -> {
                drawCircle(ink, radius=w*.17f, center=Offset(w*.37f,h*.5f), style=Stroke(width=w*.04f))
                drawCircle(ink, radius=w*.17f, center=Offset(w*.63f,h*.5f), style=Stroke(width=w*.04f))
                drawLine(ink,Offset(w*.47f,h*.5f),Offset(w*.53f,h*.5f),strokeWidth=w*.04f,cap=StrokeCap.Round)
            }
        }
    }
}
