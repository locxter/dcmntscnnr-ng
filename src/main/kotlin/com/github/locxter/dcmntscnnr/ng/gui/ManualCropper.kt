package com.github.locxter.dcmntscnnr.ng.gui

import org.opencv.core.Mat
import org.opencv.core.Point
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*
import javax.swing.border.EmptyBorder

class ManualCropper(parent: JFrame) : JDialog(parent, "Manual cropper", true) {
    private val panel = JPanel()
    private val constraints = GridBagConstraints()
    private val imageCropPreview = ImageCropPreview()
    private val okButton = JButton("OK")
    private val cancelButton = JButton("Cancel")
    private var status: Int = JOptionPane.OK_OPTION
    private var image: Mat = Mat()
        set(value) {
            imageCropPreview.image = value
            field = value
        }
    var topLeft: Point? = null
        get() {
            field = imageCropPreview.topLeft
            return field
        }
        private set
    var topRight: Point? = null
        get() {
            field = imageCropPreview.topRight
            return field
        }
        private set
    var bottomRight: Point? = null
        get() {
            field = imageCropPreview.bottomRight
            return field
        }
        private set
    var bottomLeft: Point? = null
        get() {
            field = imageCropPreview.bottomLeft
            return field
        }
        private set

    init {
        // Add functions to the buttons
        okButton.addActionListener {
            dispose()
            status = JOptionPane.OK_OPTION
        }
        cancelButton.addActionListener {
            dispose()
            status = JOptionPane.CANCEL_OPTION
        }
        // Create the panel
        panel.border = EmptyBorder(5, 5, 5, 5)
        panel.layout = GridBagLayout()
        constraints.insets = Insets(5, 5, 5, 5)
        constraints.fill = GridBagConstraints.BOTH
        constraints.weightx = 1.0
        constraints.weighty = 1.0
        constraints.gridx = 0
        constraints.gridy = 0
        constraints.gridwidth = 4
        panel.add(imageCropPreview, constraints)
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.weighty = 0.0
        constraints.gridx = 0
        constraints.gridy = 1
        constraints.gridwidth = 1
        panel.add(okButton, constraints)
        constraints.gridx = 1
        constraints.gridy = 1
        panel.add(cancelButton, constraints)
        // Create the dialog window
        size = Dimension(640, 640)
        minimumSize = Dimension(480, 480)
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        contentPane = panel
        isResizable = true
        pack()
        setLocationRelativeTo(parent)
    }

    constructor(parent: JFrame, image: Mat) : this(parent) {
        this.image = image
    }

    fun run(): Int {
        isVisible = true
        return status
    }
}
