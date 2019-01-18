/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework

import java.net.URL
import org.eclipse.core.runtime.FileLocator
import org.eclipse.core.runtime.IConfigurationElement
import org.eclipse.core.runtime.IExtension
import org.eclipse.core.runtime.IExtensionPoint
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.Platform
import org.eclipse.core.runtime.Status
import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.ui.IWorkbench
import org.eclipse.ui.IWorkbenchPage
import org.eclipse.ui.IWorkbenchWindow
import org.eclipse.ui.plugin.AbstractUIPlugin
import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext
import ch.hsr.ifs.testframework.model.Model
import ch.hsr.ifs.testframework.ui.FallbackImageProvider
import ch.hsr.ifs.testframework.ui.FallbackMessages

/**
 * The activator class controls the plug-in life cycle
 */
class TestFrameworkPlugin : AbstractUIPlugin() {

	private val fModel = Model()

	companion object {

		const val PLUGIN_ID = "ch.hsr.ifs.testframework"
		lateinit var default: TestFrameworkPlugin private set

		private val ICONS_PATH = Path("\$nl$/icons")

		fun getImageDescriptor(relativePath: String): ImageDescriptor =
			ImageDescriptor.createFromURL(FileLocator.find(default.bundle, ICONS_PATH.append(relativePath), null))

		val imageProvider: ImageProvider by lazy {
			try {
				val registry = Platform.getExtensionRegistry()
				registry.getExtensionPoint(PLUGIN_ID, "ImageProvider")?.run {
					extensions.map {
						it.configurationElements[0].createExecutableExtension("class") as? ImageProvider
					}.firstOrNull()
				} ?: FallbackImageProvider()
			} catch (ignored: Throwable) {
				FallbackImageProvider()
			}
		}

		val messages: Messages by lazy {
			try {
				val registry = Platform.getExtensionRegistry()
				registry.getExtensionPoint(PLUGIN_ID, "Messages")?.run {
					extensions.map {
						it.configurationElements[0].createExecutableExtension("class") as? Messages
					}.firstOrNull()
				} ?: FallbackMessages()
			} catch (ignored: Throwable) {
				FallbackMessages()
			}
		}

		val activeWorkbenchWindow: IWorkbenchWindow? get() = default.workbench.activeWorkbenchWindow

		val activePage: IWorkbenchPage? get() = activeWorkbenchWindow?.activePage

		fun log(e: Throwable) = log(Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Error", e))

		fun log(status: IStatus) = default.getLog().log(status)

		fun getModel() = default.fModel
	}

	init {
		default = this
	}

	override fun stop(context: BundleContext?) {
		super.stop(context)
	}

}