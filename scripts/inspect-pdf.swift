import Foundation
import PDFKit
import AppKit

// Local-only PDF inspection: render every page and print page/text counts.
guard CommandLine.arguments.count == 3,
      let pdf = PDFDocument(url: URL(fileURLWithPath: CommandLine.arguments[1])) else {
    fatalError("Usage: swift scripts/inspect-pdf.swift input.pdf output-directory")
}
let destination = URL(fileURLWithPath: CommandLine.arguments[2], isDirectory: true)
try FileManager.default.createDirectory(at: destination, withIntermediateDirectories: true)
print("Pages: \(pdf.pageCount)")
for index in 0..<pdf.pageCount {
    guard let page = pdf.page(at: index) else { fatalError("Missing page") }
    let box = page.bounds(for: .mediaBox)
    guard let context = CGContext(data: nil, width: Int(box.width * 1.5), height: Int(box.height * 1.5),
                                  bitsPerComponent: 8, bytesPerRow: 0, space: CGColorSpaceCreateDeviceRGB(),
                                  bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue) else { fatalError("No context") }
    context.setFillColor(NSColor.white.cgColor)
    context.fill(CGRect(x: 0, y: 0, width: box.width * 1.5, height: box.height * 1.5))
    context.scaleBy(x: 1.5, y: 1.5)
    page.draw(with: .mediaBox, to: context)
    guard let image = context.makeImage(),
          let png = NSBitmapImageRep(cgImage: image).representation(using: .png, properties: [:]) else { fatalError("Cannot render page") }
    try png.write(to: destination.appendingPathComponent(String(format: "page-%02d.png", index + 1)))
    print("Page \(index + 1): \((page.string ?? "").count) text characters")
}
