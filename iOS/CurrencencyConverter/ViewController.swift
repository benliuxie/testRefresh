//
//  ViewController.swift
//  CurrencencyConverter
//
//  Created by Ben Liu on 2017-06-14.
//  Copyright © 2017 TestBenLiu. All rights reserved.
//

import UIKit

class ViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view, typically from a nib.
    }
    
    override func viewDidAppear(_ animated: Bool) {
        //never fetched or more than 30 min since last fetch
        let timeSinceLastFetch = DataHelper.timeSinceLastRemoteFetch()
        if(timeSinceLastFetch == -1 || timeSinceLastFetch > 1800){
            DataHelper.remoteFetch() { currencies in
                DataHelper.save(currencies: currencies as Dictionary<String, AnyObject>)
                
                self.proceedToMain()
                
            }
        }else{
            self.proceedToMain()
            
        }

    }
    
    func proceedToMain(){
        let storyBoard : UIStoryboard = UIStoryboard(name: "Main", bundle:nil)
        
        let nextViewController = storyBoard.instantiateViewController(withIdentifier: "main") as! MainViewController
 
        self.present(nextViewController, animated:true, completion:nil)

    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }


}

