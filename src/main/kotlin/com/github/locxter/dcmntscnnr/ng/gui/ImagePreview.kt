package com.github.locxter.dcmntscnnr.ng.gui

import com.github.locxter.dcmntscnnr.ng.lib.OpencvHelpers
import org.opencv.core.Mat
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import javax.swing.JComponent
import kotlin.math.roundToInt

class ImagePreview() : JComponent() {
    private var aspectRatio: Double = 0.0
    private var scaledImage: Image? = null
    private var referenceHeight = 0
    var image: Mat = Mat()
        set(value) {
            aspectRatio = value.width() / value.height().toDouble()
            if (width > 0 && height > 0 && height == referenceHeight) {
                scaledImage = OpencvHelpers.matToAwtImage(value).getScaledInstance(width, height, Image.SCALE_SMOOTH)
            }
            referenceHeight = 0
            field = value
            repaint()
        }

    constructor(image: Mat) : this() {
        this.image = image
    }

    // Method to draw the component
    override fun paintComponent(context: Graphics) {
        // Clear the component
        super.paintComponent(context)
        val context2d = context as Graphics2D
        // Draw the image
        context2d.drawImage(scaledImage, 0, 0, width, height, null)
        // Resize if needed
        if (height != referenceHeight) {
            val newWidth = (aspectRatio * height).roundToInt()
            scaledImage = OpencvHelpers.matToAwtImage(image).getScaledInstance(newWidth, height, Image.SCALE_SMOOTH)
            preferredSize = Dimension(newWidth, 0)
            referenceHeight = height
            revalidate()
        }
    }
}
