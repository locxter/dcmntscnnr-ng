package com.github.locxter.dcmntscnnr.ng.gui

import com.github.locxter.dcmntscnnr.ng.model.Document
import com.github.locxter.dcmntscnnr.ng.model.EPosition
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.border.EmptyBorder

class DocumentEditor() : JScrollPane() {
    var document = Document()
        get() {
            field.pages.clear()
            for (pageEditor in pageEditors) {
                field.pages.add(pageEditor.page)
            }
            return field
        }
        set(value) {
            pageEditors.clear()
            for (i in value.pages.indices) {
                pageEditors.add(PageEditor(value.pages[i]))
            }
            assemblePanel()
            field = value
        }
    private val pageEditors: MutableList<PageEditor> = mutableListOf()
    private val panel = JPanel()
    private val constraints = GridBagConstraints()

    init {
        // Configure the scroll pane
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED)
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER)
        panel.border = EmptyBorder(5, 5, 5, 5)
        panel.layout = GridBagLayout()
        constraints.insets = Insets(5, 5, 5, 5)
        constraints.fill = GridBagConstraints.VERTICAL
        constraints.weighty = 1.0
        assemblePanel()
    }

    constructor(document: Document) : this() {
        this.document = document
    }

    private fun assemblePanel() {
        panel.removeAll()
        for (i in 0 until pageEditors.size) {
            constraints.gridx = i
            constraints.gridy = 0
            pageEditors[i].position = if (pageEditors.size > 1) {
                when (i) {
                    0 -> EPosition.POSITION_LEFT
                    pageEditors.lastIndex -> EPosition.POSITION_RIGHT
                    else -> EPosition.POSITION_MIDDLE
                }
            } else {
                EPosition.POSITION_ALONE
            }
            pageEditors[i].removeRemoveButtonActionListeners()
            pageEditors[i].addRemoveButtonActionListener {
                pageEditors.remove(pageEditors[i])
                assemblePanel()
            }
            pageEditors[i].removeMoveLeftButtonActionListeners()
            pageEditors[i].removeMoveRightButtonActionListeners()
            if (pageEditors[i].position.hasMoveLeft) {
                pageEditors[i].addMoveLeftButtonActionListener {
                    val buffer = pageEditors[i - 1].page
                    pageEditors[i - 1].page = pageEditors[i].page
                    pageEditors[i].page = buffer
                }
            }
            if (pageEditors[i].position.hasMoveRight) {
                pageEditors[i].addMoveRightButtonActionListener {
                    val buffer = pageEditors[i + 1].page
                    pageEditors[i + 1].page = pageEditors[i].page
                    pageEditors[i].page = buffer
                }
            }
            panel.add(pageEditors[i], constraints)
        }
        setViewportView(panel)
    }
}
