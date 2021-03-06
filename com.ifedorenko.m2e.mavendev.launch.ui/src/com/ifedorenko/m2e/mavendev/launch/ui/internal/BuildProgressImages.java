package com.ifedorenko.m2e.mavendev.launch.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class BuildProgressImages {

  private static final BuildProgressActivator UIPLUGIN = BuildProgressActivator.getInstance();

  public static interface ILazyImage {

    Image get();

    ImageDescriptor getDescriptor();

  }

  public static final ILazyImage PROJECT = create("pom.gif");

  public static final ILazyImage PROJECT_INPROGRESS =
      createDecorated("pom.gif", "inprogress_ovr.png");

  public static final ILazyImage PROJECT_SUCCESS = createDecorated("pom.gif", "success_ovr.gif");

  public static final ILazyImage PROJECT_FAILURE = createDecorated("pom.gif", "failure_ovr.png");

  public static final ILazyImage PROJECT_SKIPPED = createDecorated("pom.gif", "skipped_ovr.png");

  public static final ILazyImage FAILURE = create("failure.gif");

  public static final ILazyImage PROGRESSVIEW = create("progressview.png");

  public static final ILazyImage PROGRESSVIEW_FAILURE = create("progressview_failure.png");

  public static final ILazyImage PROGRESSVIEW_INPROGRESS = create("progressview_inprogress.png");

  public static final ILazyImage PROGRESSVIEW_SUCCESS = create("progressview_success.png");

  //
  // boring implementation follows
  //

  private static class SimpleImage implements ILazyImage {
    private final String filename;

    public SimpleImage(String filename) {
      this.filename = filename;
    }

    @Override
    public Image get() {
      return getImageRegistry().get(filename);
    }

    @Override
    public ImageDescriptor getDescriptor() {
      return getImageRegistry().getDescriptor(filename);
    }
  }

  private static class DecoratedImage implements ILazyImage {

    private final ILazyImage baseImage;

    private final ILazyImage overlayImage;

    public DecoratedImage(ILazyImage baseImage, ILazyImage overlayImage) {
      this.baseImage = baseImage;
      this.overlayImage = overlayImage;
    }

    @Override
    public Image get() {
      return UIPLUGIN.getResourceManager().createImage(getDescriptor());
    }

    @Override
    public ImageDescriptor getDescriptor() {
      ImageDescriptor[] overlays = new ImageDescriptor[5];
      overlays[IDecoration.BOTTOM_LEFT] = overlayImage.getDescriptor();
      return new DecorationOverlayIcon(baseImage.get(), overlays);
    }
  }

  private static SimpleImage create(String filename) {
    ImageRegistry registry = getImageRegistry();
    if (registry == null) {
      return null;
    }
    if (registry.getDescriptor(filename) == null) {
      ImageDescriptor descriptor = AbstractUIPlugin
          .imageDescriptorFromPlugin(BuildProgressActivator.PLUGINID, "icons/" + filename);
      registry.put(filename, descriptor);
    }
    return new SimpleImage(filename);
  }

  private static ILazyImage createDecorated(String baseFilename, String overlayFilename) {
    SimpleImage baseImage = create(baseFilename);
    SimpleImage overlayImage = create(overlayFilename);
    return new DecoratedImage(baseImage, overlayImage);
  }


  private static ImageRegistry getImageRegistry() {
    if (UIPLUGIN == null) {
      return null;
    }
    return UIPLUGIN.getImageRegistry();
  }
}
