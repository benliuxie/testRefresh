//
//  MainViewController.swift
//  CurrencencyConverter
//
//  Created by Ben Liu on 2017-06-14.
//  Copyright © 2017 TestBenLiu. All rights reserved.
//

import UIKit
import CoreData
class MainViewController: UIViewController , UICollectionViewDataSource,UIPopoverPresentationControllerDelegate,PopoverSelectionDelegate,  NSFetchedResultsControllerDelegate, UITextFieldDelegate{

    @IBOutlet weak var valueField: UITextField!
    
    @IBOutlet weak var currencyChooserBtn: UIButton!
    
    @IBOutlet weak var collectionConverted: UICollectionView!
    let reuseIdentifier = "cell"
    var currencies : [Currency] = []
    
    var ratio:Float = 1.0
    
    
    @IBAction func CurrencyButtonClick(_ sender: UIButton) {
        let popoverContent:PopOverTableViewController = self.storyboard?.instantiateViewController(withIdentifier: "popoverTable") as! PopOverTableViewController
        popoverContent.delegate = self
        popoverContent.modalPresentationStyle = .popover
        popoverContent.preferredContentSize = CGSize(width: ((self.view.frame.width / 5) * 2), height: self.view.frame.height);        popoverContent.popoverPresentationController?.permittedArrowDirections = .any//UIPopoverArrowDirection.up
        popoverContent.popoverPresentationController?.sourceView = sender
        popoverContent.popoverPresentationController?.sourceRect=sender.bounds
        popoverContent.setData(cur: currencies)
        popoverContent.popoverPresentationController?.delegate = self

        present(popoverContent, animated: true, completion: nil)

        
    }

    
    
    func adaptivePresentationStyle(for controller: UIPresentationController, traitCollection: UITraitCollection) -> UIModalPresentationStyle {
        // return UIModalPresentationStyle.FullScreen
        return UIModalPresentationStyle.none
    }


    
    func popoverSelected(index:Int){
        let cur:Currency = currencies[index]
        DataHelper.setBase(base: [cur.name!:1.0])
        self.currencyChooserBtn.setTitle(cur.name!, for: .normal)
        self.valueField.text = "1.0"
        self.dismiss(animated: true, completion: nil)
        updateDisplay()
        DataHelper.checkUpdate(){
            result in
            if result {
                print("upated")
                self.currencies = DataHelper.localFetchAll()
                self.collectionConverted.reloadData()
            } else {
                print("not updated")
            }
        }


    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.valueField.delegate = self
        self.currencies = DataHelper.localFetchAll()
        self.collectionConverted.reloadData()
        DataHelper.shared.fetchedResultsController.delegate = self
        
    }
    
    
    
    func controller(_ controller: NSFetchedResultsController<NSFetchRequestResult>, didChange anObject: Any, at indexPath: IndexPath?, for type: NSFetchedResultsChangeType, newIndexPath: IndexPath?) {
        print("TestChange didChange anObject")

    }
    
    
    func controllerDidChangeContent(_ controller: NSFetchedResultsController<NSFetchRequestResult>) {
        print("TestChange controllerDidChangeContent")

        currencies = DataHelper.localFetchAll()
        self.collectionConverted.reloadData()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        let base:[String:Float] = DataHelper.getBase()
        let baseC:String = base.keys.first!
        let baseV:String = base.values.first!.description
        self.currencyChooserBtn.setTitle(baseC, for: .normal)
        self.valueField.text = baseV
        
        self.valueField.addTarget(self, action: #selector(textFieldDidChange(_:)), for: .editingChanged)
        updateDisplay()
    }
    
    
    func textFieldDidChange(_ textField: UITextField) {
        updateDisplay()
        
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.endEditing(true)
        return true
    }
    
        
    func updateDisplay(){
        let baseStr:String = self.currencyChooserBtn.title(for: .normal)!
        let valueStr:String = self.valueField.text!.trimmingCharacters(in: NSCharacterSet.whitespaces)
        
        if let value:Float = Float(valueStr){
            if(value > 0){
                for cur in currencies{
                    if(cur.name == baseStr){
                        self.ratio = value/cur.rate
                        break
                    }
                }
                self.collectionConverted.reloadData()
            }
        }else{
            print("notnumber")//Not number
        }
        
    }
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return self.currencies.count
    }
    
    // make a cell for each cell index path
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        
        // get a reference to our storyboard cell
        let cell: MyCollectionViewCell = collectionView.dequeueReusableCell(withReuseIdentifier: reuseIdentifier, for: indexPath) as! MyCollectionViewCell
        
        // Use the outlet in our custom class to get a reference to the UILabel in the cell
        let cur = self.currencies[indexPath.row] 
        cell.currencyName.text = cur.name
        cell.currencyRate.text = (cur.rate * self.ratio).description
        cell.backgroundColor = UIColor.cyan // make cell more visible in our example project
        
        return cell
    }
    
    

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
