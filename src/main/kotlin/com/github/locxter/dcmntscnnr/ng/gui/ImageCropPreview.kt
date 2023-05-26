package com.github.locxter.dcmntscnnr.ng.gui

import com.github.locxter.dcmntscnnr.ng.lib.OpencvHelpers
import org.opencv.core.Mat
import org.opencv.core.Point
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.Path2D
import javax.swing.JComponent
import kotlin.math.max
import kotlin.math.roundToInt

class ImageCropPreview() : JComponent() {
    private var scalingRatio = 0.0
    private var scaledImage: Image? = null
    private var referenceWidth = 0
    private var referenceHeight = 0
    var image: Mat = Mat()
        set(value) {
            if (scalingRatio > 0.0) {
                scaledImage = OpencvHelpers.matToAwtImage(value).getScaledInstance(
                    getScaledValue(value.width()),
                    getScaledValue(value.height()),
                    Image.SCALE_SMOOTH
                )
            }
            topLeft = Point(0.0, 0.0)
            topRight = Point(value.width().toDouble(), 0.0)
            bottomRight = Point(value.width().toDouble(), value.height().toDouble())
            bottomLeft = Point(0.0, value.height().toDouble())
            field = value
            repaint()
        }
    var topLeft: Point? = null
        private set
    var topRight: Point? = null
        private set
    var bottomRight: Point? = null
        private set
    var bottomLeft: Point? = null
        private set

    init {
        this.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(event: MouseEvent) {
                if (scaledImage != null) {
                    val xOffset = ((width - getScaledValue(image.width())) / 2.0).roundToInt()
                    val yOffset = ((height - getScaledValue(image.height())) / 2.0).roundToInt()
                    if (event.x > xOffset && event.x < width - xOffset &&
                        event.y > yOffset && event.y < height - yOffset
                    ) {
                        val effectiveX = event.x - xOffset
                        val effectiveY = event.y - yOffset
                        val effectiveWidth = width - (2 * xOffset)
                        val effectiveHeight = height - (2 * yOffset)
                        val clickX = (event.x - xOffset) * (1 / scalingRatio)
                        val clickY = (event.y - yOffset) * (1 / scalingRatio)
                        if (effectiveX < effectiveWidth / 2.0 && effectiveY < effectiveHeight / 2.0) {
                            // Top left
                            topLeft = Point(clickX, clickY)
                        } else if (effectiveX >= effectiveWidth / 2.0 && effectiveY < effectiveHeight / 2.0) {
                            // Top right
                            topRight = Point(clickX, clickY)
                        } else if (effectiveX >= effectiveWidth / 2.0 && effectiveY >= effectiveHeight / 2.0) {
                            // Bottom right
                            bottomRight = Point(clickX, clickY)
                        } else {
                            // Bottom left
                            bottomLeft = Point(clickX, clickY)
                        }
                        repaint()
                    }
                }
            }
        })
        minimumSize = Dimension(400, 400)
        preferredSize = Dimension(400, 400)
        revalidate()
    }

    constructor(image: Mat) : this() {
        this.image = image
    }

    // Method to draw the component
    override fun paintComponent(context: Graphics) {
        // Clear the component
        super.paintComponent(context)
        val context2d = context as Graphics2D
        // Calculate the scaling ratio and center the canvas
        if (width.toDouble() / height > image.width().toDouble() / image.height()) {
            scalingRatio = height / image.height().toDouble()
            context2d.translate(((width - getScaledValue(image.width()).toDouble()) / 2).roundToInt(), 0)
        } else {
            scalingRatio = width / image.width().toDouble()
            context2d.translate(0, ((height - getScaledValue(image.height()).toDouble()) / 2).roundToInt())
        }
        // Draw the image
        context2d.drawImage(scaledImage, 0, 0, getScaledValue(image.width()), getScaledValue(image.height()), null)
        if (topLeft != null && topRight != null && bottomRight != null && bottomLeft != null) {
            // Draw the selected area
            val path = Path2D.Double()
            path.moveTo(topLeft!!.x * scalingRatio, topLeft!!.y * scalingRatio)
            path.lineTo(topRight!!.x * scalingRatio, topRight!!.y * scalingRatio)
            path.lineTo(bottomRight!!.x * scalingRatio, bottomRight!!.y * scalingRatio)
            path.lineTo(bottomLeft!!.x * scalingRatio, bottomLeft!!.y * scalingRatio)
            path.closePath()
            context2d.color = Color(0, 255, 0, 64)
            context2d.fill(path)
            // Draw the corners
            context2d.color = Color(0, 255, 0)
            val radius = max(getScaledValue((max(width, height) * 0.005).roundToInt()), 2)
            context2d.fillOval(
                getScaledValue(topLeft!!.x.roundToInt()) - radius,
                getScaledValue(topLeft!!.y.roundToInt()) - radius,
                radius * 2,
                radius * 2
            )
            context2d.fillOval(
                getScaledValue(topRight!!.x.roundToInt()) - radius,
                getScaledValue(topRight!!.y.roundToInt()) - radius,
                radius * 2,
                radius * 2
            )
            context2d.fillOval(
                getScaledValue(bottomRight!!.x.roundToInt()) - radius,
                getScaledValue(bottomRight!!.y.roundToInt()) - radius,
                radius * 2,
                radius * 2
            )
            context2d.fillOval(
                getScaledValue(bottomLeft!!.x.roundToInt()) - radius,
                getScaledValue(bottomLeft!!.y.roundToInt()) - radius,
                radius * 2,
                radius * 2
            )
        }
        // Resize if needed
        if (width != referenceWidth || height != referenceHeight) {
            scaledImage = OpencvHelpers.matToAwtImage(image)
                .getScaledInstance(getScaledValue(image.width()), getScaledValue(image.height()), Image.SCALE_SMOOTH)
            referenceWidth = width
            referenceHeight = height
            repaint()
        }
    }

    // Helper method to transform an unscaled value to a scaled one
    private fun getScaledValue(unscaledValue: Int): Int {
        return (unscaledValue * scalingRatio).roundToInt()
    }
}
