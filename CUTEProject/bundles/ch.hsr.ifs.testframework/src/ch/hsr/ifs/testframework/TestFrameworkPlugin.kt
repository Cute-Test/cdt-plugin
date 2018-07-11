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

		var default: TestFrameworkPlugin? = null

		private val ICONS_PATH = Path("\$nl$/icons")

		val uniqueIdentifier get() = default?.apply { bundle.symbolicName } ?: PLUGIN_ID

		fun getImageDescriptor(relativePath: String): ImageDescriptor = createImageDescriptor(default!!.getBundle(), ICONS_PATH.append(relativePath))

		val imageProvider: ImageProvider
			get() {
				try {
					Platform.getExtensionRegistry().getExtensionPoint(TestFrameworkPlugin.PLUGIN_ID, "ImageProvider")?.let {
						it.extensions.forEach {
							val configElements = it.configurationElements
							val className = configElements[0].getAttribute("class")
							default?.bundle?.loadClass(className)?.getDeclaredConstructor()?.apply {
								return newInstance() as ImageProvider
							}
						}
					}
				} catch (ignored: Exception) {
				}
				return FallbackImageProvider()
			}

		val activeWorkbenchWindow: IWorkbenchWindow? get() = default?.workbench?.activeWorkbenchWindow

		val activePage: IWorkbenchPage? get() = activeWorkbenchWindow?.activePage

		val messages: Messages?
			get() {
				try {
					Platform.getExtensionRegistry().getExtensionPoint(TestFrameworkPlugin.PLUGIN_ID, "Messages")?.let{
						it.extensions.forEach{
							val configElements = it.configurationElements
							val className = configElements!![0].getAttribute("class")
							default?.bundle?.loadClass(className)?.getDeclaredConstructor()?.apply{
								return newInstance() as Messages
							}
						}
					}
				} catch (ignored: Exception) {
				}
				return FallbackMessages()
			}

		private fun createImageDescriptor(bundle: Bundle?, path: IPath?): ImageDescriptor {
			val url = FileLocator.find(bundle, path, null)
			return ImageDescriptor.createFromURL(url)
		}

		fun log(e: Throwable) = log(Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Error", e))

		fun log(status: IStatus) = default!!.getLog().log(status)

		fun getModel() = default?.fModel
	}

	init {
		default = this
	}

	override fun stop(context: BundleContext?) {
		default = null
		super.stop(context)
	}

}