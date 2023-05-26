package com.github.locxter.dcmntscnnr.ng.model

import com.github.locxter.dcmntscnnr.ng.lib.OpencvHelpers
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

class Page() {
    var rawImage: Mat = Mat()
        set(value) {
            field = value
            state = EState.STATE_RAW
        }
    val croppedImage: Mat = Mat()
    val binarizedImage: Mat = Mat()
    var state: EState = EState.STATE_UNINITIALIZED
        private set

    constructor(rawImage: Mat) : this() {
        this.rawImage = rawImage
    }

    fun overrideState(state: EState) {
        if (state != EState.STATE_UNINITIALIZED && state.ordinal < this.state.ordinal) {
            this.state = state
        }
    }

    fun rotateLeft() {
        for (i in EState.STATE_RAW.ordinal..state.ordinal) {
            val image = when (i) {
                EState.STATE_RAW.ordinal -> rawImage
                EState.STATE_CROPPED.ordinal -> croppedImage
                EState.STATE_BINARIZED.ordinal -> binarizedImage
                else -> rawImage
            }
            Core.rotate(image, image, Core.ROTATE_90_COUNTERCLOCKWISE)
        }
    }

    fun rotateRight() {
        for (i in EState.STATE_RAW.ordinal..state.ordinal) {
            val image = when (i) {
                EState.STATE_RAW.ordinal -> rawImage
                EState.STATE_CROPPED.ordinal -> croppedImage
                EState.STATE_BINARIZED.ordinal -> binarizedImage
                else -> rawImage
            }
            Core.rotate(image, image, Core.ROTATE_90_CLOCKWISE)
        }
    }

    fun manualCrop(topLeft: Point, topRight: Point, bottomRight: Point, bottomLeft: Point) {
        if (state == EState.STATE_RAW) {
            // Calculate utility values for perspective transform
            val sourceCoordinates = MatOfPoint2f()
            val destinationCoordinates = MatOfPoint2f()
            val topWidth = sqrt((topRight.x - topLeft.x).pow(2.0) + (topRight.y - topLeft.y).pow(2.0))
            val bottomWidth = sqrt((bottomRight.x - bottomLeft.x).pow(2.0) + (bottomRight.y - bottomLeft.y).pow(2.0))
            val width = max(topWidth, bottomWidth)
            val leftHeight = sqrt((bottomLeft.x - topLeft.x).pow(2.0) + (bottomLeft.y - topLeft.y).pow(2.0))
            val rightHeight = sqrt((bottomRight.x - topRight.x).pow(2.0) + (bottomRight.y - topRight.y).pow(2.0))
            val height = max(leftHeight, rightHeight)
            sourceCoordinates.fromArray(
                *arrayOf(
                    topLeft,
                    topRight,
                    bottomRight,
                    bottomLeft
                )
            )
            destinationCoordinates.fromArray(
                *arrayOf(
                    Point(0.0, 0.0),
                    Point(width, 0.0),
                    Point(width, height),
                    Point(0.0, height)
                )
            )
            // Perform perspective transform
            val transformationMatrix = Imgproc.getPerspectiveTransform(sourceCoordinates, destinationCoordinates)
            Imgproc.warpPerspective(rawImage, croppedImage, transformationMatrix, Size(width, height))
            state = EState.STATE_CROPPED
        }
    }

    fun crop() {
        if (state == EState.STATE_RAW) {
            val preprocessedImage = rawImage.clone()
            val cannyImage = Mat()
            // Image processing variables
            val contours: MutableList<MatOfPoint> = mutableListOf()
            var approximation = MatOfPoint2f()
            val sortedApproximation: Array<Point?> = arrayOfNulls(4)
            val sourceCoordinates = MatOfPoint2f()
            val destinationCoordinates = MatOfPoint2f()
            // Preprocess for edge detection
            Imgproc.cvtColor(preprocessedImage, preprocessedImage, Imgproc.COLOR_BGR2GRAY)
            Imgproc.medianBlur(preprocessedImage, preprocessedImage, 25)
            Imgproc.adaptiveThreshold(
                preprocessedImage, preprocessedImage, 255.0,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 75, 0.0
            )
            Imgproc.medianBlur(preprocessedImage, preprocessedImage, 25)
            // Perform canny edge detection
            Imgproc.Canny(preprocessedImage, cannyImage, 0.0, 0.0)
            // Find contours and their approximations in order to select the largest one
            Imgproc.findContours(cannyImage, contours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
            val contours2f = contours.map { OpencvHelpers.matOfPointToMatOfPoint2f(it) }
            var largestArea = 0.0
            for (contour2f in contours2f) {
                val epsilon = 0.05 * Imgproc.arcLength(contour2f, true)
                val approximationCandidate2f = MatOfPoint2f()
                Imgproc.approxPolyDP(contour2f, approximationCandidate2f, epsilon, true)
                val area = Imgproc.contourArea(approximationCandidate2f)
                if (area > largestArea) {
                    val approximationCandidate = OpencvHelpers.matOfPoint2fToMatOfPoint(approximationCandidate2f)
                    if (Imgproc.isContourConvex(approximationCandidate) && approximationCandidate.rows() == 4) {
                        approximation = approximationCandidate2f
                        largestArea = area
                    }
                }
            }
            // Order the points for perspective transform
            val approxArray = approximation.toArray()
            val xAverage = (approxArray[0].x + approxArray[1].x + approxArray[2].x + approxArray[3].x) / 4.0
            val yAverage = (approxArray[0].y + approxArray[1].y + approxArray[2].y + approxArray[3].y) / 4.0
            for (point in approxArray) {
                if (point.x < xAverage && point.y < yAverage) {
                    // Top left
                    sortedApproximation[0] = Point(point.x, point.y)
                } else if (point.x > xAverage && point.y < yAverage) {
                    // Top right
                    sortedApproximation[1] = Point(point.x, point.y)
                } else if (point.x > xAverage && point.y > yAverage) {
                    // Bottom right
                    sortedApproximation[2] = Point(point.x, point.y)
                } else {
                    // Bottom left
                    sortedApproximation[3] = Point(point.x, point.y)
                }
            }
            // Calculate utility values for perspective transform
            val topWidth = sqrt(
                (sortedApproximation[1]!!.x - sortedApproximation[0]!!.x).pow(2.0)
                        + (sortedApproximation[1]!!.y - sortedApproximation[0]!!.y).pow(2.0)
            )
            val bottomWidth = sqrt(
                (sortedApproximation[2]!!.x - sortedApproximation[3]!!.x).pow(2.0)
                        + (sortedApproximation[2]!!.y - sortedApproximation[3]!!.y).pow(2.0)
            )
            val width = max(topWidth, bottomWidth)
            val leftHeight = sqrt(
                (sortedApproximation[3]!!.x - sortedApproximation[0]!!.x).pow(2.0)
                        + (sortedApproximation[3]!!.y - sortedApproximation[0]!!.y).pow(2.0)
            )
            val rightHeight = sqrt(
                (sortedApproximation[2]!!.x - sortedApproximation[1]!!.x).pow(2.0)
                        + (sortedApproximation[2]!!.y - sortedApproximation[1]!!.y).pow(2.0)
            )
            val height = max(leftHeight, rightHeight)
            sourceCoordinates.fromArray(*sortedApproximation)
            destinationCoordinates.fromArray(
                *arrayOf(
                    Point(0.0, 0.0),
                    Point(width, 0.0),
                    Point(width, height),
                    Point(0.0, height)
                )
            )
            // Perform perspective transform
            val transformationMatrix = Imgproc.getPerspectiveTransform(sourceCoordinates, destinationCoordinates)
            Imgproc.warpPerspective(rawImage, croppedImage, transformationMatrix, Size(width, height))
            state = EState.STATE_CROPPED
        }
    }

    fun binarize() {
        if (state == EState.STATE_CROPPED) {
            val processedImage = croppedImage.clone()
            Imgproc.cvtColor(processedImage, processedImage, Imgproc.COLOR_BGR2GRAY)
            Imgproc.adaptiveThreshold(
                processedImage, binarizedImage, 255.0,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 125, 25.0
            )
            state = EState.STATE_BINARIZED
        }
    }
}
