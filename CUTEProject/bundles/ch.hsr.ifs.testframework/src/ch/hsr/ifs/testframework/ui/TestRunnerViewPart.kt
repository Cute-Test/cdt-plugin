/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.ui

import ch.hsr.ifs.testframework.TestFrameworkPlugin
import ch.hsr.ifs.testframework.model.ISessionListener
import ch.hsr.ifs.testframework.model.TestSession
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.debug.core.DebugException
import org.eclipse.debug.core.model.IProcess
import org.eclipse.debug.ui.DebugUITools
import org.eclipse.jface.action.Action
import org.eclipse.jface.action.IAction
import org.eclipse.jface.action.Separator
import org.eclipse.jface.viewers.TreeViewer
import org.eclipse.swt.SWT
import org.eclipse.swt.events.ControlEvent
import org.eclipse.swt.events.ControlListener
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.ui.part.ViewPart
import org.eclipse.ui.progress.UIJob


public class TestRunnerViewPart : ViewPart(), ISessionListener {

	companion object {
		const val ID = "ch.hsr.ifs.cutelauncher.ui.TestRunnerViewPart"
		val msg = TestFrameworkPlugin.getMessages()
	}

	private enum class Orientation {
		horizontal, vertical
	}

	private lateinit var top: Composite
	private lateinit var TopPanel: Composite
	protected var autoScroll = true
	protected lateinit var counterPanel: CounterPanel
	private lateinit var cuteProgressBar: CuteProgressBar
	private lateinit var testViewer: TestViewer
	private lateinit var parent: Composite
	private var currentOrientation = Orientation.horizontal
	private lateinit var scrollLockAction: ScrollLockAction
	private lateinit var failureOnlyAction: FailuresOnlyFilterAction
	private lateinit var showNextFailureAction: ShowNextFailureAction
	private lateinit var showPreviousFailureAction: ShowPreviousFailureAction
	private var session: TestSession? = null
	private lateinit var stopAction: StopAction
	private lateinit var rerunSelectedAction: RerunSelectedAction

	init {
		TestFrameworkPlugin.getModel().addListener(this)
	}

	val rerunLastTestAction: IAction by lazy {
		RerunLastTestAction(this).apply {
			setEnabled(false)
		}
	}

	override fun createPartControl(parent: Composite) {
		this.parent = parent
		addResizeListener(parent)
		val gridLayout = GridLayout()
		gridLayout.numColumns = 1
		gridLayout.marginWidth = 0
		gridLayout.horizontalSpacing = 0
		gridLayout.marginWidth = 0
		gridLayout.marginHeight = 0
		val gdata = GridData()
		gdata.grabExcessHorizontalSpace = true
		top = Composite(parent, SWT.NONE)
		top.setLayout(gridLayout)
		top.setLayoutData(gdata)
		setPartName(msg.getString("TestRunnerViewPart.Name"))
		createTopPanel()
		createTestViewer()
		configureToolbar()
		getSite().setSelectionProvider(testViewer.getTreeViewer())
	}

	private fun addResizeListener(parent: Composite) {
		parent.addControlListener(object : ControlListener {

			override fun controlMoved(e: ControlEvent) = Unit

			override fun controlResized(e: ControlEvent) {
				computeOrientation()
			}
		})
	}

	val isCreated get() = this::counterPanel.isInitialized

	private fun configureToolbar() {
		val actionBars = getViewSite().getActionBars()
		val toolBar = actionBars.getToolBarManager()

		scrollLockAction = ScrollLockAction(this)
		scrollLockAction.setChecked(!autoScroll)

		failureOnlyAction = FailuresOnlyFilterAction()
		failureOnlyAction.setChecked(false)

		showNextFailureAction = ShowNextFailureAction(this)
		showNextFailureAction.setEnabled(false)
		showPreviousFailureAction = ShowPreviousFailureAction(this)
		showPreviousFailureAction.setEnabled(false)

		stopAction = StopAction()
		stopAction.setEnabled(false)

		toolBar.add(showNextFailureAction)
		toolBar.add(showPreviousFailureAction)
		toolBar.add(failureOnlyAction)
		toolBar.add(scrollLockAction)
		toolBar.add(Separator())
		toolBar.add(rerunLastTestAction)
		toolBar.add(stopAction)
	}

	private fun createTopPanel() {
		val gridLayout1 = GridLayout()
		gridLayout1.numColumns = 2
		val gridData = GridData()
		gridData.grabExcessHorizontalSpace = true
		gridData.verticalAlignment = org.eclipse.swt.layout.GridData.FILL
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL
		TopPanel = Composite(top, SWT.NONE)
		createCounterPanel()
		TopPanel.setLayout(gridLayout1)
		TopPanel.setLayoutData(gridData)
		createCuteProgressBar()
	}

	private fun createCounterPanel() {
		val gridData1 = GridData()
		gridData1.grabExcessHorizontalSpace = true
		gridData1.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL
		counterPanel = CounterPanel(TopPanel, SWT.NONE)
		counterPanel.setLayoutData(gridData1)
	}

	private fun createCuteProgressBar() {
		val gridData2 = GridData()
		gridData2.grabExcessHorizontalSpace = true
		gridData2.horizontalIndent = 35
		gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL
		cuteProgressBar = CuteProgressBar(TopPanel)
		cuteProgressBar.setLayoutData(gridData2)
	}

	private fun createTestViewer() {
		val gridData3 = GridData()
		gridData3.grabExcessHorizontalSpace = true
		gridData3.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL
		gridData3.verticalAlignment = org.eclipse.swt.layout.GridData.FILL
		gridData3.grabExcessVerticalSpace = true
		testViewer = TestViewer(top, SWT.NONE, this)
		testViewer.setLayoutData(gridData3)
	}

	private fun computeOrientation() {
		val size = parent.getSize()
		if (size.x != 0 && size.y != 0) {
			if (size.x > size.y) setOrientation(Orientation.horizontal)
			else setOrientation(Orientation.vertical)
		}
	}

	private fun setOrientation(orientation: Orientation) {
		testViewer.setOrientation(orientation == Orientation.horizontal)
		currentOrientation = orientation
		val layout = TopPanel.getLayout() as GridLayout
		setCounterColumns(layout)
		parent.layout()
	}

	private fun setCounterColumns(layout: GridLayout) = when (currentOrientation) {
		Orientation.horizontal -> layout.numColumns = 2
		else -> layout.numColumns = 1
	}


	override fun setFocus() = Unit

	var isAutoScroll
		get() = autoScroll
		set(value) {
			autoScroll = value
		}

	private inner class SessionFinishedUIJob(name: String) : UIJob(name) {

		override fun runInUIThread(monitor: IProgressMonitor): IStatus {
			rerunLastTestAction.setEnabled(true)
			stopAction.setEnabled(false)
			if (this@TestRunnerViewPart.session?.hasErrorOrFailure() ?: false) {
				showNextFailureAction.setEnabled(true)
				showPreviousFailureAction.setEnabled(true)
				if (isAutoScroll) {
					testViewer.selectFirstFailure()
				}
			}
			return Status(IStatus.OK, TestFrameworkPlugin.PLUGIN_ID, IStatus.OK, msg.getString("TestRunnerViewPart.OK"), null)
		}
	}

	private inner class FailuresOnlyFilterAction : Action(msg.getString("TestRunnerViewPart.ShowFailuresOnly"), AS_CHECK_BOX) {

		init {
			setToolTipText(msg.getString("TestRunnerViewPart.ShowFailuresOnly"))
			setImageDescriptor(TestFrameworkPlugin.getImageDescriptor("obj16/failures.gif"))
		}

		override fun run() = setShowFailuresOnly(isChecked())
	}

	private inner class StopAction : Action() {

		init {
			setText(msg.getString("TestRunnerViewPart.StopCuteTestRun"))
			setToolTipText(msg.getString("TestRunnerViewPart.StopCuteTestRun"))
			setDisabledImageDescriptor(TestFrameworkPlugin.getImageDescriptor("dlcl16/stop.gif"))
			setHoverImageDescriptor(TestFrameworkPlugin.getImageDescriptor("obj16/stop.gif"))
			setImageDescriptor(TestFrameworkPlugin.getImageDescriptor("obj16/stop.gif"))
		}

		override fun run() = stopTest()
	}

	fun setShowFailuresOnly(b: Boolean) {
		testViewer.failuresOnly = b
	}

	fun stopTest(): Unit = try {
		session?.launch?.processes?.forEach(IProcess::terminate) ?: Unit
	} catch (e: DebugException) {
	}

	fun rerunTestRun() {
		session?.apply {
			launch?.launchConfiguration?.let {
				DebugUITools.launch(it, this.launch.launchMode)
			}
		}
	}

	fun rerunSelectedTestRun(rerunnames: List<String>) {
		session?.apply {
			launch?.launchConfiguration?.let {
				try {
					val copy = it.copy("")
					val builder = StringBuilder()
					rerunnames.forEach {
						builder.append(" \"$it\"")
					}
					copy.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, builder.toString())
					copy
				} catch (e: CoreException) {
					TestFrameworkPlugin.log(e)
					it
				}?.let {
					DebugUITools.launch(it, this.launch.launchMode)
				}
			}
		}
	}

	fun selectNextFailure() {
		testViewer.selectNextFailure()
	}

	fun selectPrevFailure() {
		testViewer.selectPrevFailure()
	}

	override fun sessionFinished(session: TestSession) {
		val sessionFinishedUIJob = SessionFinishedUIJob(msg.getString("TestRunnerViewPart.SessionOver"))
		sessionFinishedUIJob.schedule()
	}

	override fun sessionStarted(session: TestSession) {
		this.session = session
		showNextFailureAction.setEnabled(false)
		showPreviousFailureAction.setEnabled(false)
		rerunLastTestAction.setEnabled(false)
		stopAction.setEnabled(true)
	}

	fun getRerunSelectedAction(treeViewer: TreeViewer): RerunSelectedAction {
		if (!this::rerunSelectedAction.isInitialized) {
			rerunSelectedAction = RerunSelectedAction(this, treeViewer)
		}
		return rerunSelectedAction
	}

}
