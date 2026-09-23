package com.example.freeinvoicegeneratorbydaybookcloud.pdf

internal data class InvoiceTemplateAsset(
    val id: String,
    val title: String,
    val category: String,
    val path: String
)

internal object InvoiceTemplateAssets {
    private val paths = listOf(
        "templates/beauty-and-wellness/rose-quartz-invoice-template.html",
        "templates/blue/royal-blue/blue-invoice-cgst-sgst.html",
        "templates/blue/royal-blue/blue-invoice-igst.html",
        "templates/blue/royal-blue/blue-invoice-non-taxable.html",
        "templates/boutique/rose-clay-invoice-template.html",
        "templates/buissness/amber-edge-invoice-template.html",
        "templates/buissness/aqua-edge-invoice-template.html",
        "templates/buissness/bold-edge-invoice-template.html",
        "templates/buissness/indigo-stream-invoice-template.html",
        "templates/corporate/charcoal-line-invoice-template.html",
        "templates/corporate/navy-ledger-invoice-template.html",
        "templates/creative/aqua-slate-invoice-template.html",
        "templates/creative/blush-grid-invoice-template.html",
        "templates/creative/copper-flame-invoice-template.html",
        "templates/creative/rainbow-mint-invoice-template.html",
        "templates/creative/royal-stripe-invoice-template.html",
        "templates/creative/studio-luxe-invoice-template.html",
        "templates/elegant/blue-ledger-invoice-template.html",
        "templates/elegant/indigo-mark-invoice-template.html",
        "templates/elegant/mint-divide-invoice-template.html",
        "templates/elegant/plum-accent-invoice-template.html",
        "templates/elegant/slate-edge-invoice-template.html",
        "templates/freelancer/black-red.html",
        "templates/freelancer/copper-elegance.html",
        "templates/freelancer/creative-blue-and-yellow.html",
        "templates/freelancer/creative-wave.html",
        "templates/freelancer/crystal-blue.html",
        "templates/freelancer/dark-modern.html",
        "templates/freelancer/elegant.html",
        "templates/freelancer/elegant-dark.html",
        "templates/freelancer/emerald-flow.html",
        "templates/freelancer/factura.html",
        "templates/freelancer/fresh.html",
        "templates/freelancer/golden-edge.html",
        "templates/freelancer/gradient-wave.html",
        "templates/freelancer/greenleaf-invoice-template.html",
        "templates/freelancer/jade-stripe.html",
        "templates/freelancer/leaf-pattern-blue.html",
        "templates/freelancer/luxury-gold.html",
        "templates/freelancer/minimalist.html",
        "templates/freelancer/minimal-pink.html",
        "templates/freelancer/modern-dark.html",
        "templates/freelancer/modern-gradient.html",
        "templates/freelancer/modern-green.html",
        "templates/freelancer/modern-purple.html",
        "templates/freelancer/neon-blue.html",
        "templates/freelancer/pastel.html",
        "templates/freelancer/polygon-tech.html",
        "templates/freelancer/premium-curved.html",
        "templates/freelancer/purple-clover.html",
        "templates/freelancer/retro.html",
        "templates/freelancer/rose-blush.html",
        "templates/freelancer/royal-blue-gradient.html",
        "templates/freelancer/sunset-blaze.html",
        "templates/freelancer/sweet-shop.html",
        "templates/freelancer/vertical-split.html",
        "templates/freelancer/warm-gradient.html",
        "templates/green/pale-green/green-invoice-cgst-sgst.html",
        "templates/green/pale-green/green-invoice-igst.html",
        "templates/green/pale-green/green-invoice-non-taxable.html",
        "templates/healthcare/mint-balance-invoice-template.html",
        "templates/modern/amber-craft-invoice-template.html",
        "templates/modern/amber-stripe-invoice-template.html",
        "templates/modern/aqua-breeze-invoice-template.html",
        "templates/modern/aqua-glow-invoice-template.html",
        "templates/modern/blush-wave-invoice-template.html",
        "templates/modern/cobalt-form-invoice-template.html",
        "templates/modern/modern-blue-invoice-template.html",
        "templates/modern/mono-core-invoice-template.html",
        "templates/modern/ocean-edge-invoice-template.html",
        "templates/modern/skyline-invoice-template.html",
        "templates/modern/slate-gold-invoice-template.html",
        "templates/modern/teal-edge-invoice-template.html",
        "templates/modern/teal-mark-invoice-template.html",
        "templates/modern/verdant-edge-invoice-template.html",
        "templates/modern/violet-ledger-invoice-template.html",
        "templates/orange/tangerine/tangerine-orange-invoice.html",
        "templates/professional/violet-crest-invoice-template.html",
        "templates/red/scarlet/scarlet-red-invoice-cgst-sgst.html",
        "templates/red/scarlet/scarlet-red-invoice-igst.html",
        "templates/red/scarlet/scarlet-red-invoice-non-taxable.html",
        "templates/retail/sunflare-invoice-template.html",
        "templates/simple-design/abstract-pastel.html",
        "templates/simple-design/amber-stripe.html",
        "templates/simple-design/azure-classic.html",
        "templates/simple-design/balloon-fun.html",
        "templates/simple-design/bold-accent-business.html",
        "templates/simple-design/cheerful.html",
        "templates/simple-design/classic-cream.html",
        "templates/simple-design/classic-minimal.html",
        "templates/simple-design/clean-professional-red.html",
        "templates/simple-design/colorful-gradient.html",
        "templates/simple-design/contemporary-business.html",
        "templates/simple-design/contemporary-purple.html",
        "templates/simple-design/coral-breeze.html",
        "templates/simple-design/corporate-sidebar.html",
        "templates/simple-design/crimson-edge.html",
        "templates/simple-design/curved-gradient.html",
        "templates/simple-design/deep-sea.html",
        "templates/simple-design/eco-green.html",
        "templates/simple-design/editorial-edge.html",
        "templates/simple-design/elegant-gray.html",
        "templates/simple-design/elegant-wave.html",
        "templates/simple-design/essential-healthcare.html",
        "templates/simple-design/fresh-curved.html",
        "templates/simple-design/fresh-mint.html",
        "templates/simple-design/fresh-modern.html",
        "templates/simple-design/geometric-modern.html",
        "templates/simple-design/geometric-shapes.html",
        "templates/simple-design/gradient-sidebar.html",
        "templates/simple-design/healthcare-invoice.html",
        "templates/simple-design/healthchrono.html",
        "templates/simple-design/holotech.html",
        "templates/simple-design/kids-care.html",
        "templates/simple-design/lavender-kids.html",
        "templates/simple-design/lavender-stripe.html",
        "templates/simple-design/mediprime-split.html",
        "templates/simple-design/neotech.html",
        "templates/simple-design/opulence.html",
        "templates/simple-design/peachaura.html",
        "templates/simple-design/playful-baby.html",
        "templates/simple-design/purple-blaze.html",
        "templates/simple-design/purple-geometric-elegance.html",
        "templates/simple-design/royal-indigo.html",
        "templates/simple-design/ruby-luxe.html",
        "templates/simple-design/sidebar-slate-invoice-template.html",
        "templates/simple-design/split-gradient.html",
        "templates/simple-design/steel-blue.html",
        "templates/simple-design/sunburst-grey.html",
        "templates/simple-design/sunset-gradient.html",
        "templates/simple-design/tropical-sunset.html",
        "templates/simple-design/turquoise.html",
        "templates/simple-design/verdant-glow.html",
        "templates/simple-design/vibrant-duo.html",
        "templates/simple-design/vibrant-edge.html",
        "templates/simple-design/vibrant-gradient.html",
        "templates/simple-design/vibrant-star-gradient.html",
        "templates/simple-design/vintage-gazette.html",
        "templates/simple-design/vintage-luxe.html",
        "templates/simple-design/violet-gradient.html",
        "templates/simple-design/vitalcare.html",
        "templates/simple-design/warm-beige.html",
        "templates/simple-design/warm-glow.html",
        "templates/simple-design/watercolor-gradient.html",
        "templates/simple-design/wellness.html",
        "templates/simple-design/yellow-accent.html",
        "templates/startup/blue-split-invoice-template.html",
        "templates/startup/inferno-line-invoice-template.html",
        "templates/startup/poppins-breeze-invoice-template.html",
        "templates/tech-service/bluecrest-invoice-template.html",
        "templates/tech-service/elegant-gold-invoice-template.html",
        "templates/tech-service/modern-circle-invoice-template.html",
        "templates/tech-service/royal-plum-invoice-template.html",
        "templates/tech-service/teal-flow-invoice-template.html"
    )

    private val curatedIdsByPath = mapOf(
        "templates/modern/teal-mark-invoice-template.html" to "modern_teal",
        "templates/simple-design/coral-breeze.html" to "coral_breeze",
        "templates/simple-design/crimson-edge.html" to "crimson_edge",
        "templates/simple-design/ruby-luxe.html" to "ruby_luxe",
        "templates/simple-design/violet-gradient.html" to "violet_gradient",
        "templates/simple-design/watercolor-gradient.html" to "watercolor_gradient",
        "templates/startup/blue-split-invoice-template.html" to "blue_split",
        "templates/startup/inferno-line-invoice-template.html" to "inferno_line",
        "templates/startup/poppins-breeze-invoice-template.html" to "poppins_breeze",
        "templates/tech-service/bluecrest-invoice-template.html" to "bluecrest",
        "templates/tech-service/elegant-gold-invoice-template.html" to "elegant_gold",
        "templates/tech-service/modern-circle-invoice-template.html" to "modern_circle",
        "templates/tech-service/royal-plum-invoice-template.html" to "royal_plum",
        "templates/tech-service/teal-flow-invoice-template.html" to "teal_flow",
        "templates/elegant/indigo-mark-invoice-template.html" to "center_mark",
        "templates/simple-design/sidebar-slate-invoice-template.html" to "left_rail",
        "templates/modern/amber-stripe-invoice-template.html" to "stripe_classic",
        "templates/retail/sunflare-invoice-template.html" to "total_focus",
        "templates/buissness/amber-edge-invoice-template.html" to "boxed_meta",
        "templates/simple-design/classic-minimal.html" to "minimal_letter",
        "templates/freelancer/vertical-split.html" to "split_brand",
        "templates/corporate/navy-ledger-invoice-template.html" to "ledger_pro",
        "templates/creative/studio-luxe-invoice-template.html" to "studio_card",
        "templates/corporate/charcoal-line-invoice-template.html" to "corporate_panel",
        "templates/simple-design/contemporary-business.html" to "classic_business",
        "templates/freelancer/minimalist.html" to "minimal_black",
        "templates/freelancer/modern-green.html" to "soft_green",
        "templates/freelancer/luxury-gold.html" to "premium_gold",
        "templates/elegant/slate-edge-invoice-template.html" to "corporate_slate",
        "templates/freelancer/creative-wave.html" to "creative_coral",
        "templates/elegant/blue-ledger-invoice-template.html" to "clean_ledger"
    )

    val all: List<InvoiceTemplateAsset> = paths.map { path ->
        InvoiceTemplateAsset(
            id = curatedIdsByPath[path] ?: idFromPath(path),
            title = titleFromPath(path),
            category = categoryFromPath(path),
            path = path
        )
    }

    fun pathFor(id: String): String? = all.firstOrNull { it.id == normalizeLegacyId(id) }?.path

    private fun normalizeLegacyId(id: String): String = when (id) {
        "elegant_blue" -> "blue_split"
        "royal_purple" -> "royal_plum"
        else -> id
    }

    private fun idFromPath(path: String): String =
        path.removePrefix("templates/")
            .removeSuffix(".html")
            .replace(Regex("[^A-Za-z0-9]+"), "_")
            .trim('_')
            .lowercase()

    private fun titleFromPath(path: String): String =
        path.substringAfterLast('/')
            .removeSuffix(".html")
            .removeSuffix("-invoice-template")
            .removeSuffix("-invoice")
            .replace('-', ' ')
            .split(' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

    private fun categoryFromPath(path: String): String =
        path.removePrefix("templates/")
            .substringBefore('/')
            .replace('-', ' ')
            .replaceFirstChar { it.uppercase() }
}
