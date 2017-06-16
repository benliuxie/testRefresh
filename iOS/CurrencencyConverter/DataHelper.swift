//
//  DataHelper.swift
//  CurrencencyConverter
//
//  Created by Ben Liu on 2017-06-14.
//  Copyright © 2017 TestBenLiu. All rights reserved.
//

import Foundation
import CoreData
import UIKit

class DataHelper{
    static let DefaultBaseKey = "defaultbase"
    static let DefaultDateKey = "defaultdate"
    
    static let shared = DataHelper()
    var fetchedResultsController : NSFetchedResultsController<NSFetchRequestResult>!
    
    private init(){
        guard let appDelegate =
            UIApplication.shared.delegate as? AppDelegate else {
                return
        }
        
        let managedContext =
            appDelegate.persistentContainer.viewContext
        
        //2
        let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "Currency")
        fetchRequest.sortDescriptors = [NSSortDescriptor(key: "name", ascending: true)]
        
        self.fetchedResultsController = NSFetchedResultsController(fetchRequest: fetchRequest, managedObjectContext: managedContext, sectionNameKeyPath: nil, cacheName: "rootCache")
        do {
            try fetchedResultsController.performFetch()
        } catch {
            fatalError("Failed to initialize FetchedResultsController: \(error)")
        }

    }
    


    
    static func remoteFetch(completion: @escaping (([String:Any]) -> ())){
        let session = URLSession.shared
        let fetchURL = URL(string: "https://api.fixer.io/latest")!
        session.dataTask(with: fetchURL) { (data, response, error) -> Void in            //var currencies: [Restaurant] = []
            if error != nil{
                //print(error)
            }else{
                
                let json = try? JSONSerialization.jsonObject(with: data!) as! [String:Any]
                var currencies = json?["rates"] as! [String:Any]
                let base = json?["base"] as! String
                currencies[base] = 1.0
                
                if(DataHelper.getBase().count == 0){
                    DataHelper.setBase(base: [base:1.0])
                }
                DataHelper.setLastRemoteFetchDate()
                completion(currencies)
                
            }
            }.resume()
    }
    
    
    static func save(currencies:Dictionary<String,AnyObject>) {
        
        guard let appDelegate =
            UIApplication.shared.delegate as? AppDelegate else {
                return
        }
        
        // 1
        let managedContext =
            appDelegate.persistentContainer.viewContext
        
        for (name, rate) in currencies{
            do {
                let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "Currency")
                fetchRequest.predicate = NSPredicate(format: "name == %@", name)
                let list = try managedContext.fetch(fetchRequest) as? [NSManagedObject]
                //var results: NSArray = managedContext.executeFetchRequest(fetchRequest, error: nil)!
                if((list?.count)! > 0){
                    let currency = list![0] as! Currency
                    if(currency.rate != rate.floatValue){
                        currency.setValue(rate, forKeyPath: "rate")
                    }
                    
                    
                }else{
                    
                    let entity =
                        NSEntityDescription.entity(forEntityName: "Currency",
                                                   in: managedContext)!
                    
                    let currency = NSManagedObject(entity: entity,
                                                   insertInto: managedContext)
                    
                    // 3
                    currency.setValue(name, forKeyPath: "name")
                    currency.setValue(rate, forKeyPath: "rate")
                    print(name+" :")
                }
            } catch let error as NSError {
                // failure
                print("Fetch failed: \(error.localizedDescription)")
            }
        }
        
        
        // 4
        do {
            try managedContext.save()
            //people.append(person)
        } catch let error as NSError {
            print("Could not save. \(error), \(error.userInfo)")
        }
    }
    
    
    static func localFetchAll() ->[Currency]{
        
        var currencies: [NSManagedObject] = []
        //1
        guard let appDelegate =
            UIApplication.shared.delegate as? AppDelegate else {
                return []
        }
        
        let managedContext =
            appDelegate.persistentContainer.viewContext
        
        //2
        let fetchRequest =
            NSFetchRequest<NSManagedObject>(entityName: "Currency")
        fetchRequest.sortDescriptors = [NSSortDescriptor(key: "name", ascending: true)]
        
        //3
        do {
            currencies = try managedContext.fetch(fetchRequest)
        } catch let error as NSError {
            print("Could not fetch. \(error), \(error.userInfo)")
        }
        
        return currencies as! [Currency]
    }
    
    
    
    static func setBase(base:[String:Float]){
        UserDefaults.standard.setValue(base, forKey: DefaultBaseKey)
    }
    
    static func getBase()->[String:Float]{
        let result = UserDefaults.standard.value(forKey: DefaultBaseKey)
        if(result == nil){
            return [:]
        }
        return result as! Dictionary<String,Float>
    }
    
    static func setLastRemoteFetchDate(){
        UserDefaults.standard.set(Date(), forKey:DefaultDateKey)
    }
    
    static func timeSinceLastRemoteFetch() -> Double{
        let date = UserDefaults.standard.object(forKey: DefaultDateKey)
        if(date == nil){
            return -1.0
        }
        let elapsedTime = NSDate().timeIntervalSince(date as! Date)
        
        print("secondsSinceLastRemoteFetch\(elapsedTime)")
        return elapsedTime
    }
    
    static func checkUpdate(completion:(Bool) -> ()) {
        let timeSinceLastFetch = DataHelper.timeSinceLastRemoteFetch()
        if(timeSinceLastFetch == -1 || timeSinceLastFetch > 1800){
            DataHelper.remoteFetch() { currencies in
                DataHelper.save(currencies: currencies as Dictionary<String, AnyObject>)
            }
            completion(true)
        }
        completion(false)

    }
    
}
