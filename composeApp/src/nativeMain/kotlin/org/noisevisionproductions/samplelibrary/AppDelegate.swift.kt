package org.noisevisionproductions.samplelibrary

import samplelibrary.KotlinAppDelegate

@UIApplicationMain
class AppDelegate : UIResponder, UIApplicationDelegate {

    private var window: UIWindow? = null
    private var kotlinAppDelegate = KotlinAppDelegate()

    func application(_ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool
    {

        kotlinAppDelegate.setup() // Call the setup method

        window = UIWindow(frame: UIScreen. main . bounds)
        let initialViewController : UIViewController
                if kotlinAppDelegate.authService.isUserLoggedIn() {
                    initialViewController =
                        SamplesViewController()  // Replace with your main view controller
                } else {
                    initialViewController =
                        LoginViewController()    // Replace with your login view controller
                }

        window?.rootViewController = initialViewController
        window?.makeKeyAndVisible()

        return true
    }
}
