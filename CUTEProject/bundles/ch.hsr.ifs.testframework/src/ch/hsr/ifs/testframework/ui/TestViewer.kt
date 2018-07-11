/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.ui

import java.util.AbstractList
import java.util.ArrayList
import java.util.Collections

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.jface.action.IMenuManager
import org.eclipse.jface.action.MenuManager
import org.eclipse.jface.action.Separator
import org.eclipse.jface.viewers.AbstractTreeViewer
import org.eclipse.jface.viewers.ISelection
import org.eclipse.jface.viewers.StructuredSelection
import org.eclipse.jface.viewers.TreeSelection
import org.eclipse.jface.viewers.TreeViewer
import org.eclipse.jface.viewers.Viewer
import org.eclipse.jface.viewers.ViewerFilter
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.SashForm
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.events.MouseAdapter
import org.eclipse.swt.events.MouseEvent
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Menu
import org.eclipse.ui.progress.UIJob

import ch.hsr.ifs.testframework.Messages
import ch.hsr.ifs.testframework.TestFrameworkPlugin
import ch.hsr.ifs.testframework.model.ISessionListener
import ch.hsr.ifs.testframework.model.ITestComposite
import ch.hsr.ifs.testframework.model.ITestCompositeListener
import ch.hsr.ifs.testframework.model.ITestElementListener
import ch.hsr.ifs.testframework.model.NotifyEvent
import ch.hsr.ifs.testframework.model.TestCase
import ch.hsr.ifs.testframework.model.TestElement
import ch.hsr.ifs.testframework.model.TestSession
import ch.hsr.ifs.testframework.model.TestStatus
import ch.hsr.ifs.testframework.model.TestSuite
import ch.hsr.ifs.testframework.model.NotifyEvent.EventType
import kotlin.properties.Delegates


public class TestViewer(parent: Composite, style: Int, private val viewPart: TestRunnerViewPart) : Composite(parent, style), ITestElementListener, ISessionListener, ITestCompositeListener {

	companion object {
		private val msg = TestFrameworkPlugin.messages!!
	}

	private inner class UpdateTestElement(name: String, private val element: TestElement, private val reveal: Boolean) : UIJob(name) {
		override fun runInUIThread(monitor: IProgressMonitor): IStatus {
			treeViewer.refresh(element, true)
			if (reveal && viewPart.isAutoScroll) {
				treeViewer.reveal(element)
			}
			return Status(IStatus.OK, TestFrameworkPlugin.PLUGIN_ID, IStatus.OK, msg.getString("TestViewer.OK"), null)
		}
	}

	private inner class ShowNewTest(name: String, private val parent: ITestComposite?, private val element: TestElement) : UIJob(name) {
		override fun runInUIThread(monitor: IProgressMonitor): IStatus {
			treeViewer.refresh(parent, true)
			if (viewPart.isAutoScroll) {
				treeViewer.reveal(element)
			}
			return Status(IStatus.OK, TestFrameworkPlugin.PLUGIN_ID, IStatus.OK, msg.getString("TestViewer.OK"), null)
		}
	}

	private inner class TestResultViewer(parent: Composite, style: Int) : StyledText(parent, style) {
		private inner class TestResultDClickListener : MouseAdapter() {
			override fun mouseDoubleClick(e: MouseEvent) {
				val case = getTreeSelection()
				if (case != null) {
					val action = CuteCompareResultAction(case, this@TestViewer.shell)
					action.run()
				}
			}

		}

		init {
			addMouseListener(TestResultDClickListener())
		}

		fun showTestDetail(test: TestElement) {
			if (test is TestCase) {
				testResultViewer.setText(test.getMessage())
				redraw()
			} else if (test is TestSuite) {
				testResultViewer.setText("")
				redraw()
			}
		}
	}

	private inner class FailuresOnlyFilter : ViewerFilter() {

		override fun select(viewer: Viewer, parentElement: Any, element: Any): Boolean = when (element) {
			is TestElement -> when (element.getStatus()) {
				TestStatus.running, TestStatus.error, TestStatus.failure -> true
				else -> false
			}
			else -> true
		}

	}

	private lateinit var sashForm: SashForm
	private lateinit var treeViewer: TreeViewer
	private lateinit var testResultViewer: TestResultViewer
	private lateinit var cuteTestDClickListener: CuteTestDClickListener
	private lateinit var rerunAction: RerunSelectedAction

	private lateinit var session: TestSession

	private val failuresOnlyFilter = FailuresOnlyFilter()
	private val elemets = mutableListOf<TestElement>()

	var failuresOnly: Boolean by Delegates.observable(false) { _, _, _ ->
		updateFilters()
	}

	init {
		TestFrameworkPlugin.getModel()?.addListener(this)
		initialize()
		addDisposeListener { _ -> TestFrameworkPlugin.getModel()?.removeListener(this) }
	}

	fun reset(session: TestSession) {
		testResultViewer.setText("")
		treeViewer.setInput(session)
	}

	fun showTestDetails(testElement: TestElement) {
		testResultViewer.showTestDetail(testElement)
	}

	private fun initialize() {
		val gridLayout = GridLayout()
		gridLayout.numColumns = 2
		gridLayout.marginWidth = 0
		gridLayout.horizontalSpacing = 0
		gridLayout.marginWidth = 1
		gridLayout.marginHeight = 0
		createSashForm()
		this.setLayout(gridLayout)
	}

	private fun createSashForm() {
		sashForm = SashForm(this, SWT.HORIZONTAL)
		sashForm.setLayoutData(GridData(GridData.FILL_BOTH))
		treeViewer = TreeViewer(sashForm, SWT.MULTI or SWT.FLAT)
		treeViewer.setContentProvider(CuteTestTreeContentProvieder())
		treeViewer.setLabelProvider(CuteTestLabelProvider())
		treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS)
		treeViewer.addSelectionChangedListener(CuteTestSelecetionListener(this))
		cuteTestDClickListener = CuteTestDClickListener(session)
		treeViewer.addDoubleClickListener(cuteTestDClickListener)
		initContextMenu()
		testResultViewer = TestResultViewer(sashForm, SWT.WRAP)
		testResultViewer.setEditable(false)
		testResultViewer.setIndent(5)
	}

	private fun handleMenuAboutToShow() {
		rerunAction.setEnabled(true)
	}

	private fun initContextMenu() {
		val menuMgr = MenuManager("#PopupMenu")
		menuMgr.addMenuListener { _ -> handleMenuAboutToShow() }
		viewPart.getViewSite().registerContextMenu(menuMgr, this.treeViewer)
		val menu = menuMgr.createContextMenu(treeViewer.getTree())
		treeViewer.getTree().setMenu(menu)

		menuMgr.add(Separator())
		rerunAction = viewPart.getRerunSelectedAction(treeViewer)
		menuMgr.add(rerunAction)
		menuMgr.add(viewPart.rerunLastTestAction)
		menuMgr.add(Separator())
	}

	override fun modelCanged(source: TestElement, event: NotifyEvent) = when (event.type) {
		EventType.suiteFinished -> UpdateTestElement(msg.getString("TestViewer.ShowNewTest") ?: "Show New Test", event.element, false)
		EventType.testFinished -> UpdateTestElement(msg.getString("TestViewer.UpdateTest") ?: "Update Test", event.element, true)
	}.schedule()

	override fun sessionStarted(session: TestSession) {
		this.session = session
		session.addListener(this)
		cuteTestDClickListener.setSession(session)
		object : UIJob(msg.getString("TestViewer.ResetTestViewer")) {

			override fun belongsTo(family: Any) = TestFrameworkPlugin.PLUGIN_ID.equals(family)

			override fun runInUIThread(monitor: IProgressMonitor): IStatus {
				reset(this@TestViewer.session)
				return Status(IStatus.OK, TestFrameworkPlugin.PLUGIN_ID, IStatus.OK, msg.getString("TestViewer.OK"), null)
			}

		}.schedule()
	}

	override fun sessionFinished(session: TestSession) = Unit

	fun updateFilters() {
		if (failuresOnly) {
			treeViewer.addFilter(failuresOnlyFilter)
		} else {
			treeViewer.removeFilter(failuresOnlyFilter)
		}
	}

	fun selectNextFailure() {
		if (session.hasErrorOrFailure()) {
			val element = getSelectedElement()
			when (element) {
				is TestCase -> treeViewer.setSelection(StructuredSelection(findNextFailure(element)), true)
				else -> treeViewer.selection = StructuredSelection(findFirstFailure())
			}
		}
	}

	private fun getSelectedElement(): Any {
		val selection = treeViewer.getSelection() as StructuredSelection
		return selection.getFirstElement()
	}

	protected fun getTreeSelection(): TestCase? {
		val sel = treeViewer.getSelection()
		if (sel is TreeSelection && sel.firstElement is TestCase) {
			return sel.firstElement as TestCase
		}
		return null
	}

	fun selectFirstFailure() {
		val findFirstFailure = findFirstFailure()
		if (findFirstFailure != null) {
			treeViewer.setSelection(StructuredSelection(findFirstFailure), true)
		}
	}

	private fun findFirstFailure(): TestElement? {
		session.getElements().forEach {
			@Suppress("NON_EXHAUSTIVE_WHEN")
			when (it.getStatus()) {
				TestStatus.failure, TestStatus.error -> {
					if (it is ITestComposite) {
						return findNextChildFailure(it, false)
					} else {
						return it
					}
				}
			}
		}
		return null
	}

	private fun getSession(): TestSession {
		if (!this::session.isInitialized) {
			session = TestFrameworkPlugin.getModel()!!.getSession()
		}
		return session
	}

	private fun findNextChildFailure(composite: ITestComposite, revese: Boolean): TestElement? {
		var elements = composite.getElements()
		if (revese) {
			//clone original list so its elements do not get reordered.
			elements = elements.toList()
			Collections.reverse(elements)
		}

		elements.forEach {
			@Suppress("NON_EXHAUSTIVE_WHEN")
			when (it.getStatus()) {
				TestStatus.failure, TestStatus.error -> {
					if (it is ITestComposite) {
						return findNextChildFailure(it, false)
					} else {
						return it
					}
				}
			}
		}
		return null
	}

	private fun findNextFailure(selected: TestCase): TestElement {
		val nextFailure = findNextSiblingFailure(selected, false)
		if (nextFailure != null) {
			return nextFailure
		} else {
			return findNextFailureInParent(selected.getParent(), selected, false)
		}
	}

	private fun findNextFailureInParent(current: ITestComposite?, selected: TestCase, reverse: Boolean): TestElement {
		if (current is TestElement) {
			val nextFailure = findNextSiblingFailure(current, reverse)
			if (nextFailure != null) {
				return nextFailure
			} else {
				return findNextFailureInParent(current.getParent(), selected, reverse)
			}
		} else {
			return selected
		}
	}

	private fun findNextSiblingFailure(tElement: TestElement, reverse: Boolean): TestElement? {
		var tests = tElement.getParent()!!.getElements()
		var index = tests.indexOf(tElement) + 1
		if (reverse) {
			tests = tests.toList()
			Collections.reverse(tests)
			index = tests.size - index + 1
		}

		val it = tests.listIterator(index)
		var nextFailure: TestElement
		while (it.hasNext()) {
			nextFailure = it.next()
			if (nextFailure.getStatus() == TestStatus.failure || nextFailure.getStatus() == TestStatus.error) {
				if (nextFailure is ITestComposite) {
					return findNextChildFailure(nextFailure, reverse)
				} else {
					return nextFailure
				}
			}
		}
		return null
	}

	fun setOrientation(horizontal: Boolean) {
		if (!this::sashForm.isInitialized || sashForm.isDisposed()) {
			return
		}
		sashForm.setOrientation(if (horizontal) SWT.HORIZONTAL else SWT.VERTICAL)
	}

	fun selectPrevFailure() {
		if (getSession().hasErrorOrFailure()) {
			val firstElement = getSelectedElement()
			if (firstElement is TestCase) {
				treeViewer.setSelection(StructuredSelection(findPrevFailure(firstElement)), true)
			} else { // show first Failure
				treeViewer.setSelection(StructuredSelection(findFirstFailure()), true)
			}
		}
	}

	private fun findPrevFailure(selected: TestCase): TestElement {
		val nextFailure = findNextSiblingFailure(selected, true)
		if (nextFailure != null) {
			return nextFailure
		} else {
			return findNextFailureInParent(selected.getParent(), selected, true)
		}
	}

	override fun newTestElement(source: ITestComposite, newElement: TestElement) {
		newElement.addTestElementListener(this)
		if (newElement is ITestComposite) {
			newElement.addListener(this)
		}
		elemets += newElement
		ShowNewTest(msg.getString("TestViewer.ShowNewTest") ?: "Show New Test", newElement.getParent(), newElement).schedule()
	}

	fun getTreeViewer() = treeViewer

}
