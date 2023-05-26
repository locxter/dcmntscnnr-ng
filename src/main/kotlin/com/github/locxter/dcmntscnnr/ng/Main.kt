package com.github.locxter.dcmntscnnr.ng

import com.formdev.flatlaf.FlatDarkLaf
import com.github.locxter.dcmntscnnr.ng.gui.DocumentEditor
import com.github.locxter.dcmntscnnr.ng.model.Document
import com.github.locxter.dcmntscnnr.ng.model.Page
import nu.pattern.OpenCV
import org.opencv.imgcodecs.Imgcodecs
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.filechooser.FileNameExtensionFilter


fun main(args: Array<String>) {
    var document = Document()
    OpenCV.loadLocally()
    // Set a pleasing LaF
    try {
        UIManager.setLookAndFeel(FlatDarkLaf())
    } catch (exception: Exception) {
        println("Failed to initialize LaF.")
    }
    // UI components
    val frame = JFrame("dcmntscnnr-ng")
    val panel = JPanel()
    val constraints = GridBagConstraints()
    val openButton = JButton("Open")
    val cropButton = JButton("Crop")
    val binarizeButton = JButton("Binarize")
    val saveButton = JButton("Save")
    val documentEditor = DocumentEditor()
    val aboutLabel = JLabel("2023 locxter")
    // Add functions to the buttons and inputs
    openButton.addActionListener {
        val filter = FileNameExtensionFilter("Images", "png", "jpg", "jpeg")
        val fileChooser = JFileChooser()
        fileChooser.isMultiSelectionEnabled = true
        fileChooser.fileFilter = filter
        val option = fileChooser.showOpenDialog(frame)
        if (option == JFileChooser.APPROVE_OPTION) {
            val files = fileChooser.selectedFiles
            document = documentEditor.document
            for (file in files) {
                val rawImage = Imgcodecs.imread(file.absolutePath)
                document.pages.add(Page(rawImage))
            }
            documentEditor.document = document
        }
    }
    cropButton.addActionListener {
        document = documentEditor.document
        document.cropPages()
        documentEditor.document = document
    }
    binarizeButton.addActionListener {
        document = documentEditor.document
        document.binarizePages()
        documentEditor.document = document
    }
    saveButton.addActionListener {
        val filter = FileNameExtensionFilter("PDF documents", "pdf")
        val fileChooser = JFileChooser()
        fileChooser.fileFilter = filter
        val option = fileChooser.showSaveDialog(frame)
        if (option == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            document = documentEditor.document
            document.createPdf(file)
        }
    }
    // Create the main panel
    panel.border = EmptyBorder(5, 5, 5, 5)
    panel.layout = GridBagLayout()
    constraints.insets = Insets(5, 5, 5, 5)
    constraints.fill = GridBagConstraints.HORIZONTAL
    constraints.weightx = 1.0
    constraints.gridx = 0
    constraints.gridy = 0
    panel.add(openButton, constraints)
    constraints.gridx = 1
    constraints.gridy = 0
    panel.add(cropButton, constraints)
    constraints.gridx = 2
    constraints.gridy = 0
    panel.add(binarizeButton, constraints)
    constraints.gridx = 3
    constraints.gridy = 0
    panel.add(saveButton, constraints)
    constraints.fill = GridBagConstraints.BOTH
    constraints.weighty = 1.0
    constraints.gridx = 0
    constraints.gridy = 1
    constraints.gridwidth = 4
    panel.add(documentEditor, constraints)
    constraints.fill = GridBagConstraints.RELATIVE
    constraints.weighty = 0.0
    constraints.gridx = 0
    constraints.gridy = 2
    panel.add(aboutLabel, constraints)
    // Create the main window
    frame.size = Dimension(640, 640)
    frame.minimumSize = Dimension(480, 480)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.add(panel)
    frame.isVisible = true
}
