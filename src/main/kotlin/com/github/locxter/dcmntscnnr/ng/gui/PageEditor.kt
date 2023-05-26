package com.github.locxter.dcmntscnnr.ng.gui

import com.github.locxter.dcmntscnnr.ng.model.EPosition
import com.github.locxter.dcmntscnnr.ng.model.EState
import com.github.locxter.dcmntscnnr.ng.model.Page
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.border.EmptyBorder

class PageEditor() : JPanel() {
    var page = Page()
        set(value) {
            if (value.state != EState.STATE_UNINITIALIZED) {
                field = value
                updateImagePreview()
            }
        }
    var position: EPosition = EPosition.POSITION_MIDDLE
        set(value) {
            field = value
            assemblePanel()
        }
    private val constraints = GridBagConstraints()
    private val imagePreview = ImagePreview()
    private val removeButton = JButton("Remove")
    private val rotateLeftButton = JButton("Rotate left")
    private val rotateRightButton = JButton("Rotate right")
    private val moveLeftButton = JButton("Move left")
    private val moveRightButton = JButton("Move right")
    private val manualCropButton = JButton("Manual crop")
    private val debinarizeButton = JButton("Debinarize")

    init {
        // Add functions to the buttons
        rotateLeftButton.addActionListener {
            page.rotateLeft()
            updateImagePreview()
        }
        rotateRightButton.addActionListener {
            page.rotateRight()
            updateImagePreview()
        }
        manualCropButton.addActionListener {
            page.overrideState(EState.STATE_RAW)
            val manualCropper = ManualCropper(SwingUtilities.getWindowAncestor(this) as JFrame, page.rawImage)
            val response = manualCropper.run()
            if (response == JOptionPane.OK_OPTION && manualCropper.topLeft != null && manualCropper.topRight != null && manualCropper.bottomRight != null && manualCropper.bottomLeft != null) {
                page.manualCrop(
                    manualCropper.topLeft!!,
                    manualCropper.topRight!!,
                    manualCropper.bottomRight!!,
                    manualCropper.bottomLeft!!
                )
                updateImagePreview()
            }
        }
        debinarizeButton.addActionListener {
            page.overrideState(EState.STATE_CROPPED)
            updateImagePreview()
        }
        // Create the panel
        border = EmptyBorder(5, 5, 5, 5)
        layout = GridBagLayout()
        constraints.insets = Insets(5, 5, 5, 5)
        assemblePanel()
    }

    constructor(page: Page) : this() {
        this.page = page
    }

    constructor(position: EPosition) : this() {
        this.position = position
    }

    constructor(page: Page, position: EPosition) : this() {
        this.page = page
        this.position = position
    }

    fun addRemoveButtonActionListener(l: ActionListener) {
        removeButton.addActionListener(l)
    }

    fun removeRemoveButtonActionListeners() {
        for (actionListener in removeButton.actionListeners) {
            removeButton.removeActionListener(actionListener)
        }
    }

    fun addMoveLeftButtonActionListener(l: ActionListener) {
        if (position.hasMoveLeft) {
            moveLeftButton.addActionListener(l)
        }
    }

    fun removeMoveLeftButtonActionListeners() {
        for (actionListener in moveLeftButton.actionListeners) {
            moveLeftButton.removeActionListener(actionListener)
        }
    }

    fun addMoveRightButtonActionListener(l: ActionListener) {
        if (position.hasMoveRight) {
            moveRightButton.addActionListener(l)
        }
    }

    fun removeMoveRightButtonActionListeners() {
        for (actionListener in moveRightButton.actionListeners) {
            moveRightButton.removeActionListener(actionListener)
        }
    }

    private fun updateImagePreview() {
        imagePreview.image = when (page.state) {
            EState.STATE_RAW -> page.rawImage
            EState.STATE_CROPPED -> page.croppedImage
            EState.STATE_BINARIZED -> page.binarizedImage
            else -> page.rawImage
        }
    }

    // Helper method for assembling the panel
    private fun assemblePanel() {
        removeAll()
        constraints.fill = GridBagConstraints.BOTH
        constraints.weightx = 1.0
        constraints.weighty = 1.0
        constraints.gridx = 0
        constraints.gridy = 0
        constraints.gridwidth = 2
        add(imagePreview, constraints)
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.weighty = 0.0
        constraints.gridx = 0
        constraints.gridy = 1
        add(removeButton, constraints)
        constraints.gridx = 0
        constraints.gridy = 2
        constraints.gridwidth = 1
        add(rotateLeftButton, constraints)
        constraints.gridx = 1
        constraints.gridy = 2
        add(rotateRightButton, constraints)
        if (position != EPosition.POSITION_ALONE) {
            when (position) {
                EPosition.POSITION_LEFT -> {
                    constraints.gridx = 0
                    constraints.gridy = 3
                    constraints.gridwidth = 2
                    add(moveRightButton, constraints)
                }
                EPosition.POSITION_MIDDLE -> {
                    constraints.gridx = 0
                    constraints.gridy = 3
                    add(moveLeftButton, constraints)
                    constraints.gridx = 1
                    constraints.gridy = 3
                    add(moveRightButton, constraints)
                }
                EPosition.POSITION_RIGHT -> {
                    constraints.gridx = 0
                    constraints.gridy = 3
                    constraints.gridwidth = 2
                    add(moveLeftButton, constraints)
                }
                else -> {}
            }
            constraints.gridx = 0
            constraints.gridy = 4
            constraints.gridwidth = 1
            add(manualCropButton, constraints)
            constraints.gridx = 1
            constraints.gridy = 4
            add(debinarizeButton, constraints)
        } else {
            constraints.gridx = 0
            constraints.gridy = 3
            constraints.gridwidth = 1
            add(manualCropButton, constraints)
            constraints.gridx = 1
            constraints.gridy = 3
            add(debinarizeButton, constraints)
        }

    }
}
