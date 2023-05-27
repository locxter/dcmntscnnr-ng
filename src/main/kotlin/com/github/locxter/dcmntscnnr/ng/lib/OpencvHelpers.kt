package com.github.locxter.dcmntscnnr.ng.lib

import org.opencv.calib3d.Calib3d
import org.opencv.core.*
import org.opencv.highgui.HighGui
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.videoio.VideoCapture
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.ImageIO
import kotlin.system.exitProcess
import com.lowagie.text.Image as OpenPdfImage
import java.awt.Image as AwtImage

class OpencvHelpers {
    companion object {
        // Function to draw text on an image
        fun drawText(image: Mat, text: String): Mat {
            val buffer = image.clone()
            for (i in 0 until text.lines().size) {
                Imgproc.putText(
                    buffer,
                    text.lines()[i],
                    Point(0.0, 30 * (i + 1.0)),
                    Imgproc.FONT_HERSHEY_SIMPLEX,
                    1.0, Scalar(255.0, 255.0, 255.0)
                )
            }
            return buffer
        }

        // Function to show an image and wait for user response
        fun showImage(image: Mat, windowName: String) {
            val maxWidth = 1920.0
            val maxHeight = 1080.0
            val buffer: Mat
            // Resize the image to fit inside the bounding box if needed
            if (image.cols() > maxWidth || image.rows() > maxHeight) {
                val scale: Double = if (image.cols() > image.rows()) {
                    maxWidth / image.cols()
                } else {
                    maxHeight / image.rows()
                }
                buffer = Mat()
                Imgproc.resize(image, buffer, Size(), scale, scale)
            } else {
                buffer = image.clone()
            }
            // Show the image until the user reacts
            while (true) {
                HighGui.imshow(windowName, buffer)
                when (HighGui.waitKey(1000 / 25)) {
                    // Q
                    81 -> exitProcess(0)
                    // R
                    82 -> break
                }
            }
        }

        // Function to save a picture to disk
        fun saveImage(image: Mat, filename: String) {
            var counter = 0
            var fullFilename = "$filename-$counter.jpeg"
            while (File(fullFilename).exists()) {
                counter++
                fullFilename = "$filename-$counter.jpeg"
            }
            Imgcodecs.imwrite(fullFilename, image)
        }

        // Function to convert a MatOfPoint to a MatOfPoint2f
        fun matOfPointToMatOfPoint2f(matOfPoint: MatOfPoint): MatOfPoint2f {
            val matOfPoint2f = MatOfPoint2f()
            matOfPoint.convertTo(matOfPoint2f, CvType.CV_32F)
            return matOfPoint2f
        }

        // Function to convert a MatOfPoint2f to a MatOfPoint
        fun matOfPoint2fToMatOfPoint(matOfPoint2f: MatOfPoint2f): MatOfPoint {
            val matOfPoint = MatOfPoint()
            matOfPoint2f.convertTo(matOfPoint, CvType.CV_32S)
            return matOfPoint
        }

        // Function to convert an OpenCV Mat to an AWT BufferedImage
        fun matToAwtImage(mat: Mat): AwtImage {
            val buffer = MatOfByte()
            Imgcodecs.imencode(".png", mat, buffer)
            return ImageIO.read(ByteArrayInputStream(buffer.toArray()))
        }

        // Function to convert an OpenCV Mat to an OpenPDF Image
        fun matToOpenPdfImage(mat: Mat): OpenPdfImage {
            val buffer = MatOfByte()
            Imgcodecs.imencode(".png", mat, buffer)
            return OpenPdfImage.getInstance(buffer.toArray())
        }
    }
}
