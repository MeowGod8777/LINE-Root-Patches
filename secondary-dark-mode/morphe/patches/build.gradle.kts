group = "dev.meowgod.line"

patches {
    about {
        name = "MeowGod LINE Patches"
        description = "Small LINE patches developed from device-side compatibility research"
        source = "https://github.com/MeowGod8777/LINE-Root-Patches"
        author = "MeowGod8777"
        contact = "https://github.com/MeowGod8777"
        website = "https://github.com/MeowGod8777/LINE-Root-Patches"
        license = "Research / repository terms"
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}
