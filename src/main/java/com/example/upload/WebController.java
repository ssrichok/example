package com.example.upload;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.upload.report.MobilePrice;
import com.example.upload.report.MobilePriceJson;
import com.example.upload.storage.StorageFileNotFoundException;
import com.example.upload.storage.StorageService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Controller
public class WebController {

    private final StorageService storageService;
    private final MobileBillCalculator mobileCaculator;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Autowired
    public WebController(StorageService storageService, MobileBillCalculator mobileCaculator) {
	this.storageService = storageService;
	this.mobileCaculator = mobileCaculator;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {

	model.addAttribute("files", storageService.loadAll().map(path -> MvcUriComponentsBuilder.fromMethodName(WebController.class, "serveFile", path
		.getFileName().toString()).build().toString()).collect(Collectors.toList()));

	return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

	Resource file = storageService.loadAsResource(filename);
	return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/usageUpload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {

	try {
	    Scanner scanner = new Scanner(file.getInputStream());
	    while (scanner.hasNext()) {
		String line = scanner.nextLine().trim();
		mobileCaculator.addCalculateBill(line);
	    }
	    scanner.close();

	    File priceReport = new File(file.getOriginalFilename() + "-price.txt");
	    FileWriter writer = new FileWriter(priceReport, false);
	    Map<String, Float> reportMap = mobileCaculator.getPaymentReport();

	    ArrayList<MobilePrice> mpList = new ArrayList<MobilePrice>();
	    for (String m : reportMap.keySet()) {

		mpList.add(new MobilePrice(m, reportMap.get(m)));
	    }
	    MobilePriceJson mobilePriceJson = new MobilePriceJson(mpList);
	    writer.write(gson.toJson(mobilePriceJson));
	    writer.flush();
	    storageService.store(priceReport);
	    writer.close();

	} catch (IOException e) {
	    e.printStackTrace();
	}
	redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + file.getOriginalFilename() + "!");

	return "redirect:/";
    }

    @PostMapping("//jsonUpload")
    public String handleJsonFileUpload(@RequestParam("jsonfile") MultipartFile file, RedirectAttributes redirectAttributes) {

	MobilePriceJson mobilePriceJson = null;
	try {
	    StringBuffer stringBuffer = new StringBuffer();
	    Scanner scanner = new Scanner(file.getInputStream());
	    while (scanner.hasNext()) {

		stringBuffer.append(scanner.nextLine());
	    }
	    scanner.close();

	    mobilePriceJson = gson.fromJson(stringBuffer.toString(), MobilePriceJson.class);

	} catch (IOException e) {
	    e.printStackTrace();
	}
	redirectAttributes.addFlashAttribute("mpList", mobilePriceJson.getMpList());

	return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
	return ResponseEntity.notFound().build();
    }
}
